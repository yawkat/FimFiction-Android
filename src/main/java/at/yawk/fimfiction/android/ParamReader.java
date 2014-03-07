package at.yawk.fimfiction.android;

import android.os.Parcel;
import android.os.Parcelable;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.json.Deserializer;
import at.yawk.fimfiction.json.Serializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Yawkat
 */
public class ParamReader implements Parcelable {
    public static final Creator<ParamReader> CREATOR = new Creator<ParamReader>() {
        @Override
        public ParamReader createFromParcel(final Parcel source) {
            String title = source.readString();

            JsonObject json = new JsonParser().parse(source.readString()).getAsJsonObject();
            try {
                SearchParameters params = new Deserializer().deserializeBundle(json, SearchParameters.class);
                return new ParamReader(params, title);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ParamReader[] newArray(final int size) {
            return new ParamReader[size];
        }
    };

    private final SearchParameters parameters;
    private final String title;

    public ParamReader(final SearchParameters parameters, String title) {
        this.parameters = parameters;
        this.title = title;
    }

    public SearchParameters getParameters() {
        return parameters;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(title);

        Serializer serializer = new Serializer();
        JsonObject json = serializer.serializeBundle(parameters);
        dest.writeString(json.toString());
    }
}
