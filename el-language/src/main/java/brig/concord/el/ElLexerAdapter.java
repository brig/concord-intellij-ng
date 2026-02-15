package brig.concord.el;

import com.intellij.lexer.FlexAdapter;

public class ElLexerAdapter extends FlexAdapter {

    public ElLexerAdapter() {
        super(new ElLexer(null));
    }
}
