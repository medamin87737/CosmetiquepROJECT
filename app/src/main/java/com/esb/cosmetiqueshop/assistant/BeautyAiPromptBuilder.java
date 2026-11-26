package com.esb.cosmetiqueshop.assistant;

import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.PriceUtils;

import java.util.List;

/**
 * Construit le prompt système : l’IA ne conseille QUE les produits du catalogue app.
 */
public final class BeautyAiPromptBuilder {

    private BeautyAiPromptBuilder() {
    }

    public static String systemPrompt() {
        return "Tu es l’assistante beauté de Cosmétique Shop (Tunisie). "
                + "Tu aides le client à choisir un produit selon son besoin (peau, cheveux, maquillage…). "
                + "Règles strictes :\n"
                + "1) Conseille UNIQUEMENT parmi le catalogue fourni ci-dessous.\n"
                + "2) Cite le nom exact du produit et son prix.\n"
                + "3) Si rien ne convient, dis-le clairement sans inventer de produit.\n"
                + "4) Réponds en français, courte, claire et chaleureuse (5–8 phrases max).\n"
                + "5) Ne mentionne pas d’autres marques hors catalogue.\n"
                + "6) Ne cite jamais d’identifiant technique (ID, numéro interne).";
    }

    public static String catalogBlock(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("CATALOGUE PRODUITS DISPONIBLES :\n");
        if (products == null || products.isEmpty()) {
            sb.append("(aucun produit actif)\n");
            return sb.toString();
        }
        int n = 0;
        for (Product p : products) {
            if (p == null || !p.isActive()) {
                continue;
            }
            n++;
            sb.append("- ").append(safe(p.getName()))
                    .append(" | marque: ").append(safe(p.getBrand()))
                    .append(" | catégorie: ").append(safe(p.getCategoryName()))
                    .append(" | peau: ").append(safe(p.getSkinType()))
                    .append(" | prix: ").append(PriceUtils.formatMillimes(p.getEffectivePriceMillimes()))
                    .append(" | stock: ").append(p.getStock())
                    .append(" | desc: ").append(shorten(safe(p.getDescription()), 120))
                    .append('\n');
            if (n >= 40) {
                sb.append("(... catalogue tronqué)\n");
                break;
            }
        }
        return sb.toString();
    }

    public static String userPrompt(String catalog, String question) {
        return catalog + "\n\nQUESTION CLIENT :\n" + question.trim();
    }

    private static String safe(String s) {
        return s == null || s.trim().isEmpty() ? "—" : s.trim().replace('\n', ' ');
    }

    private static String shorten(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "…";
    }
}
