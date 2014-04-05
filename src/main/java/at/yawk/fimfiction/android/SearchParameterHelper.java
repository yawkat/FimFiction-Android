package at.yawk.fimfiction.android;

import at.yawk.fimfiction.data.Order;
import at.yawk.fimfiction.data.SearchParameters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jonas Konrad (yawkat)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchParameterHelper {
    public static final SearchParameters UNREAD;
    public static final SearchParameters FAVORITE;
    public static final SearchParameters READ_LATER;

    static {
        UNREAD = SearchParameters.createMutable()
                                 .set(SearchParameters.SearchParameter.ORDER, Order.UPDATE_DATE)
                                 .set(SearchParameters.SearchParameter.UNREAD, true)
                                 .set(SearchParameters.SearchParameter.FAVORITED, true)
                                 .immutableCopy();
        FAVORITE = SearchParameters.createMutable()
                                   .set(SearchParameters.SearchParameter.ORDER, Order.UPDATE_DATE)
                                   .set(SearchParameters.SearchParameter.FAVORITED, true)
                                   .immutableCopy();
        READ_LATER = SearchParameters.createMutable()
                                     .set(SearchParameters.SearchParameter.ORDER, Order.UPDATE_DATE)
                                     .set(SearchParameters.SearchParameter.READ_LATER, true)
                                     .immutableCopy();
    }

    public static SearchParameters getDefault() { return UNREAD; }

    public static TranslatableText getName(SearchParameters parameters) {
        if (UNREAD.equals(parameters)) { return TranslatableText.id(R.string.unread); }
        if (FAVORITE.equals(parameters)) { return TranslatableText.id(R.string.favorite); }
        if (READ_LATER.equals(parameters)) { return TranslatableText.id(R.string.readlater); }
        return TranslatableText.id(R.string.search);
    }
}
