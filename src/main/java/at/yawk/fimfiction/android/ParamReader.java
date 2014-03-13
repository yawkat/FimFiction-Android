package at.yawk.fimfiction.android;

import android.os.Parcel;
import android.os.Parcelable;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.json.Deserializer;
import at.yawk.fimfiction.json.Serializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Yawkat
 */
@RequiredArgsConstructor
@Getter
public class ParamReader implements Parcelable {
    public static final Creator<ParamReader> CREATOR = new Creator<ParamReader>() {
        @Override
        public ParamReader createFromParcel(Parcel source) {
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
        public ParamReader[] newArray(int size) {
            return new ParamReader[size];
        }
    };

    private final SearchParameters parameters;
    private final String title;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);

        Serializer serializer = new Serializer();
        JsonObject json = serializer.serializeBundle(parameters);
        dest.writeString(json.toString());
    }
}
