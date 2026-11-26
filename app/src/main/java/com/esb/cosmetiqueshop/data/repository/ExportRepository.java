package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.OrderDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.utils.AppExecutors;
import com.esb.cosmetiqueshop.utils.CsvUtils;
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.OrderStatusUi;
import com.esb.cosmetiqueshop.utils.PriceUtils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExportRepository {

    public interface BytesCallback {
        void onReady(byte[] data, String mime, String fileName);
    }

    public interface ExportCallback {
        void onReady(String preview);
    }

    private final OrderDao orderDao;
    private final Context appContext;
    private final AppExecutors executors;

    public ExportRepository(Context context) {
        appContext = context.getApplicationContext();
        orderDao = new OrderDao(new DatabaseHelper(context));
        executors = AppExecutors.getInstance();
    }

    public void buildOrdersCsvForSave(BytesCallback callback) {
        executors.diskIO().execute(() -> {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                bos.write(CsvUtils.utf8Bom());
                String header = CsvUtils.escapeCell("id") + ";"
                        + CsvUtils.escapeCell("statut") + ";"
                        + CsvUtils.escapeCell("total_dt") + ";"
                        + CsvUtils.escapeCell("date") + "\n";
                bos.write(header.getBytes(StandardCharsets.UTF_8));
                List<Order> orders = orderDao.findAll();
                for (Order o : orders) {
                    String line = CsvUtils.escapeCell(String.valueOf(o.getId())) + ";"
                            + CsvUtils.escapeCell(o.getStatus().name()) + ";"
                            + CsvUtils.escapeCell(PriceUtils.formatMillimesNoSuffix(o.getTotalMillimes())) + ";"
                            + CsvUtils.escapeCell(String.valueOf(o.getCreatedAt())) + "\n";
                    bos.write(line.getBytes(StandardCharsets.UTF_8));
                }
                byte[] data = bos.toByteArray();
                executors.mainThread().execute(() -> callback.onReady(data, "text/csv", "commandes.csv"));
            } catch (IOException e) {
                executors.mainThread().execute(() -> callback.onReady(new byte[0], "text/csv", "commandes.csv"));
            }
        });
    }

    public void buildOrdersCsvPreview(ExportCallback callback) {
        buildOrdersCsvForSave((data, mime, name) -> {
            String preview = new String(data, StandardCharsets.UTF_8);
            callback.onReady(preview);
        });
    }

    public void buildOrdersPdfForSave(BytesCallback callback) {
        executors.diskIO().execute(() -> {
            List<Order> orders = orderDao.findAll();
            PdfDocument doc = new PdfDocument();
            Paint headerPaint = new Paint();
            headerPaint.setColor(Color.parseColor("#4E0A1F"));
            Paint titlePaint = new Paint();
            titlePaint.setColor(Color.WHITE);
            titlePaint.setTextSize(16f);
            titlePaint.setFakeBoldText(true);
            Paint bodyPaint = new Paint();
            bodyPaint.setColor(Color.parseColor("#2B1B20"));
            bodyPaint.setTextSize(11f);

            int pageNum = 1;
            int y = 80;
            PdfDocument.Page page = doc.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNum).create());
            Canvas canvas = page.getCanvas();
            canvas.drawRect(0, 0, 595, 48, headerPaint);
            canvas.drawText(appContext.getString(com.esb.cosmetiqueshop.R.string.admin_orders), 24, 32, titlePaint);

            for (Order o : orders) {
                if (y > 780) {
                    doc.finishPage(page);
                    pageNum++;
                    page = doc.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNum).create());
                    canvas = page.getCanvas();
                    canvas.drawRect(0, 0, 595, 48, headerPaint);
                    canvas.drawText(appContext.getString(com.esb.cosmetiqueshop.R.string.admin_orders), 24, 32, titlePaint);
                    y = 80;
                }
                String line = "#" + o.getId() + "  " + OrderStatusUi.label(appContext, o.getStatus())
                        + "  " + PriceUtils.formatMillimes(o.getTotalMillimes())
                        + "  " + DateUtils.formatDisplay(o.getCreatedAt());
                canvas.drawText(line, 24, y, bodyPaint);
                y += 18;
            }
            doc.finishPage(page);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                doc.writeTo(bos);
            } catch (IOException e) {
                bos.reset();
            } finally {
                doc.close();
            }
            byte[] data = bos.toByteArray();
            executors.mainThread().execute(() -> callback.onReady(data, "application/pdf", "commandes.pdf"));
        });
    }

    public void buildOrderInvoiceText(long orderId, ExportCallback callback) {
        executors.diskIO().execute(() -> {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                executors.mainThread().execute(() -> callback.onReady(""));
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Commande #").append(order.getId()).append('\n');
            for (OrderItem item : order.getItems()) {
                sb.append(item.getProductName()).append(" x").append(item.getQuantity())
                        .append(" — ").append(PriceUtils.formatMillimes(item.getLineTotalMillimes()))
                        .append('\n');
            }
            sb.append("Total: ").append(PriceUtils.formatMillimes(order.getTotalMillimes()));
            executors.mainThread().execute(() -> callback.onReady(sb.toString()));
        });
    }
}