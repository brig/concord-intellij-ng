package brig.concord.psi;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.yaml.YAMLElementTypes;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CommentsProcessor {

    public static PsiComment findFirst(PsiElement fromElement) {
        PsiComment result = null;

        PsiElement element = fromElement;
        while (canProcessElement(element)) {
            if (element instanceof PsiComment comment) {
                result = comment;
            }
            element = element.getPrevSibling();
        }

        return result;
    }

    public static Iterable<PsiComment> comments(PsiElement fromElement) {
        return () -> new CommentsIterator(fromElement);
    }

    public static Stream<PsiComment> stream(PsiElement fromElement) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new CommentsIterator(fromElement), Spliterator.ORDERED), false);
    }

    private static class CommentsIterator implements Iterator<PsiComment> {

        private PsiElement currentElement;
        private PsiComment nextElement = null;

        private CommentsIterator(PsiElement currentElement) {
            this.currentElement = currentElement;
        }

        @Override
        public boolean hasNext() {
            PsiElement element = currentElement;

            nextElement = null;
            while (canProcessElement(element)) {
                if (element instanceof PsiComment comment) {
                    nextElement = comment;
                    return true;
                }
                element = element.getNextSibling();
            }
            return false;
        }

        @Override
        public PsiComment next() {
            currentElement = nextElement.getNextSibling();
            return nextElement;
        }

    }

    private static boolean canProcessElement(PsiElement element) {
        if (element == null) {
            return false;
        }

        return element instanceof PsiComment || element instanceof PsiWhiteSpace
                || YAMLElementTypes.EOL_ELEMENTS.contains(element.getNode().getElementType())
                || YAMLElementTypes.SPACE_ELEMENTS.contains(element.getNode().getElementType());
    }
}
