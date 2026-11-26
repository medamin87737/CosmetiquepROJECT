package com.esb.cosmetiqueshop.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Statuts de commande avec règles de transition métier.
 * Admin : choix libre parmi préparation / livrée / annulée / échec.
 */
public enum OrderStatus {
    EN_ATTENTE,
    CONFIRMEE,
    EN_PREPARATION,
    EN_LIVRAISON,
    LIVREE,
    ANNULEE,
    ECHEC_LIVRAISON;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    /** Statuts que l’admin peut appliquer directement. */
    private static final Set<OrderStatus> ADMIN_CHOICES = EnumSet.of(
            EN_PREPARATION, LIVREE, ANNULEE, ECHEC_LIVRAISON);

    static {
        TRANSITIONS.put(EN_ATTENTE, EnumSet.of(CONFIRMEE, ANNULEE, EN_PREPARATION));
        TRANSITIONS.put(CONFIRMEE, EnumSet.of(EN_PREPARATION, ANNULEE));
        TRANSITIONS.put(EN_PREPARATION, EnumSet.of(EN_LIVRAISON, LIVREE, ANNULEE, ECHEC_LIVRAISON));
        TRANSITIONS.put(EN_LIVRAISON, EnumSet.of(LIVREE, ECHEC_LIVRAISON, ANNULEE));
        TRANSITIONS.put(ECHEC_LIVRAISON, EnumSet.of(EN_PREPARATION, EN_LIVRAISON, LIVREE, ANNULEE));
        TRANSITIONS.put(LIVREE, EnumSet.of(EN_PREPARATION, ANNULEE, ECHEC_LIVRAISON));
        TRANSITIONS.put(ANNULEE, EnumSet.of(EN_PREPARATION));
    }

    /** Vérifie si le passage vers {@code target} est autorisé (client / flux classique). */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) {
            return false;
        }
        Set<OrderStatus> allowed = TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    /** Admin : peut fixer n’importe lequel des 4 statuts opérationnels (sauf inchangé). */
    public boolean canAdminSetTo(OrderStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return ADMIN_CHOICES.contains(target);
    }

    /** Liste ordonnée des choix admin pour le menu. */
    public static OrderStatus[] adminChoices() {
        return new OrderStatus[]{EN_PREPARATION, LIVREE, ANNULEE, ECHEC_LIVRAISON};
    }

    /** Plus d’assignation livreur dans l’app. */
    public static boolean requiresDeliveryAgent(OrderStatus target) {
        return false;
    }

    /** Statuts autorisés depuis l'état actuel. */
    public Set<OrderStatus> allowedNextStatuses() {
        Set<OrderStatus> allowed = TRANSITIONS.get(this);
        if (allowed == null || allowed.isEmpty()) {
            return Collections.emptySet();
        }
        return EnumSet.copyOf(allowed);
    }

    public static OrderStatus fromString(String value) {
        if (value == null) {
            return EN_ATTENTE;
        }
        try {
            return OrderStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return EN_ATTENTE;
        }
    }
}
