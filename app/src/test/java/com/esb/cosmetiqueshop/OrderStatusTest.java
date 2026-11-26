package com.esb.cosmetiqueshop;

import com.esb.cosmetiqueshop.model.OrderStatus;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrderStatusTest {

    @Test
    public void enAttenteCanConfirmOrCancel() {
        assertTrue(OrderStatus.EN_ATTENTE.canTransitionTo(OrderStatus.CONFIRMEE));
        assertTrue(OrderStatus.EN_ATTENTE.canTransitionTo(OrderStatus.ANNULEE));
        assertFalse(OrderStatus.EN_ATTENTE.canTransitionTo(OrderStatus.LIVREE));
    }

    @Test
    public void livreeIsTerminal() {
        assertFalse(OrderStatus.LIVREE.canTransitionTo(OrderStatus.ANNULEE));
    }
}