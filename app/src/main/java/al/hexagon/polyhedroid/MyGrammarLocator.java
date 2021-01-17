package al.hexagon.polyhedroid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

import io.noties.prism4j.GrammarLocator;
import io.noties.prism4j.Prism4j;

public class MyGrammarLocator implements GrammarLocator {

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {
        switch (language) {

            case "json":
                //return Prism_json.create(prism4j);

            // everything else is omitted

            default:
                return null;
        }
    }

    @NotNull
    @Override
    public Set<String> languages() {
        return null;
    }
}
