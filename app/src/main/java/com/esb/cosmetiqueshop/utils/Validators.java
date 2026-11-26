package com.esb.cosmetiqueshop.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

public final class Validators {

    /** Téléphone tunisien : 8 chiffres. */
    private static final Pattern PHONE_TN = Pattern.compile("^\\d{8}$");
    private static final Pattern PASSWORD_HAS_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern PASSWORD_HAS_DIGIT = Pattern.compile("\\d");

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidTnPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_TN.matcher(phone.trim()).matches();
    }

    /** Minimum 8 caractères, au moins une lettre et un chiffre. */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return PASSWORD_HAS_LETTER.matcher(password).find()
                && PASSWORD_HAS_DIGIT.matcher(password).find();
    }
}