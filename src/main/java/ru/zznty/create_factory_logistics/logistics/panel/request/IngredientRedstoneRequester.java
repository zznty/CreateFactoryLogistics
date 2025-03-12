package ru.zznty.create_factory_logistics.logistics.panel.request;

public interface IngredientRedstoneRequester {
    IngredientOrder getOrder();

    IngredientOrder getOrderContext();

    void setOrder(IngredientOrder order);

    void setOrderContext(IngredientOrder orderContext);
}
