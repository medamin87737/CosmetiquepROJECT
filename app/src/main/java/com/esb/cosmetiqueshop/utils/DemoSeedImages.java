package com.esb.cosmetiqueshop.utils;

/**
 * Images locales intégrées (drawable-nodpi) — format drawable://nom_ressource.
 * Mapping volontairement contextualisé (catégorie / type de produit).
 */
public final class DemoSeedImages {

    public static final String HERO_DRAWABLE = "drawable://hero_cosmetics";
    public static final String LOGO_DRAWABLE = "drawable://logo_rose_mark";

    /** Soin visage — masque / soin facial */
    public static final String CAT_SOIN = "drawable://img_cat_04";
    /** Maquillage — palette / pinceaux */
    public static final String CAT_MAKEUP = "drawable://img_cat_02";
    /** Corps — soins corporels */
    public static final String CAT_CORPS = "drawable://img_prod_22";
    /** Cheveux — shampooing / huile */
    public static final String CAT_CHEVEUX = "drawable://img_cat_cheveux";
    /** Parfums */
    public static final String CAT_PARFUMS = "drawable://img_cat_05";
    /** Solaire — SPF */
    public static final String CAT_SOLAIRE = "drawable://img_cat_solaire";

    public static final String[] CATEGORY_DRAWABLES = {
            CAT_SOIN, CAT_MAKEUP, CAT_CORPS, CAT_CHEVEUX, CAT_PARFUMS, CAT_SOLAIRE
    };

    /**
     * 24 produits seed dans l'ordre d'insertion DatabaseHelper :
     * 0-3 soin, 4-7 makeup, 8-11 corps, 12-15 cheveux, 16-19 parfums, 20-23 solaire.
     */
    public static final String[] PRODUCT_DRAWABLES = {
            // Soin visage
            "drawable://img_prod_08", // crème / pots soin
            "drawable://img_cat_03",  // sérum / cleanser
            "drawable://img_cat_03",  // eau micellaire
            "drawable://img_cat_04",  // masque visage
            // Maquillage
            "drawable://img_cat_02",  // fond de teint / teint
            "drawable://img_prod_07", // rouge à lèvres
            "drawable://img_prod_06", // mascara / liner
            "drawable://img_cat_02",  // palette ombres
            // Corps
            "drawable://img_prod_22", // lait corporel
            "drawable://img_prod_20", // gommage / body care
            "drawable://img_prod_08", // beurre / baume
            "drawable://img_prod_22", // huile sèche
            // Cheveux
            "drawable://img_cat_cheveux",
            "drawable://img_cat_cheveux",
            "drawable://img_cat_cheveux",
            "drawable://img_cat_cheveux",
            // Parfums
            "drawable://img_cat_05",  // eau de parfum
            "drawable://img_prod_18", // brume / parfum
            "drawable://img_prod_18", // roll-on
            "drawable://img_cat_05",  // coffret
            // Solaire
            "drawable://img_cat_solaire",
            "drawable://img_cat_solaire",
            "drawable://img_cat_solaire",
            "drawable://img_prod_20"  // après-soleil / soin
    };

    public static final String HERO_BEAUTY_URL = HERO_DRAWABLE;
    public static final String SAMPLE_HERO_MP4_URL = "";
    public static final String[] PRODUCT_URLS = PRODUCT_DRAWABLES;
    public static final String[] CATEGORY_URLS = CATEGORY_DRAWABLES;

    private DemoSeedImages() {
    }
}
