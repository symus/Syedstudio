package com.craftora.product;

public final class ProductEnums {

    private ProductEnums() {}

    public enum Status { DRAFT, PUBLISHED, ARCHIVED }

    /** Admin moderation is independent of the maker's own publication status. */
    public enum Moderation { VISIBLE, HIDDEN }

    public enum AvailabilityType { READY_MADE, MADE_TO_ORDER }

    public enum OptionType { TEXT, SELECT }

    public enum Occasion {
        BIRTHDAY, WEDDING, ANNIVERSARY, CHRISTMAS, VALENTINES, EASTER, BABY, HOUSEWARMING, MOTHERS_DAY,
        FATHERS_DAY, GRADUATION, EVERYDAY
    }
}
