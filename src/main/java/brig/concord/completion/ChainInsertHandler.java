package brig.concord.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChainInsertHandler implements InsertHandler<LookupElement> {

    private final List<InsertHandler<LookupElement>> handlers;

    public ChainInsertHandler(List<InsertHandler<LookupElement>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        handlers.forEach(h -> h.handleInsert(context, item));
    }
}
