package com.elitemastereric.writer;

import java.io.IOException;
import java.io.OutputStream;

import javax.lang.model.element.Element;

import com.elitemastereric.ElementUtils;
import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.util.DocTrees;

public class DocumentationBuilder implements DocTreeVisitor<String, Void> {

    public static final DocumentationBuilder INSTANCE = new DocumentationBuilder();

    private DocTrees docTrees = null;

    public DocTrees getDocTrees() {
        if (docTrees == null)
            throw new IllegalStateException("docTrees not set");
        return docTrees;
    }

    public void setDocTrees(DocTrees docTrees) {
        this.docTrees = docTrees;
    }

    public static void writeDocs(OutputStream out, Element element, int indent) throws IOException {
        String docs = buildDocs(element);
        
        if (docs == null) return; // Element is undocumented
        
        BaseWriter.writeIndent(out, indent);
        BaseWriter.write(out, "/**%n");
        
        String[] docsLines = docs.split("\n");
        for (String docsLine : docsLines) {
            BaseWriter.writeIndent(out, indent);
            BaseWriter.write(out, " * %s%n", docsLine);
        }

        BaseWriter.writeIndent(out, indent);
        BaseWriter.write(out, " */%n");
    }

    public static String buildDocs(Element element) throws IOException {
        DocCommentTree docCommentTree = INSTANCE.getDocTrees().getDocCommentTree(element);

        if (docCommentTree == null)
            return null;

        return docCommentTree.accept(INSTANCE, null);
    }

    @Override
    public String visitAttribute(AttributeTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCATTRIBUTE: " + node.getName() + " = " + node.getValue());
    }

    @Override
    public String visitAuthor(AuthorTree node, Void _v) {
        return ElementUtils.escapeFormat("@author " + node.getName());
    }

    @Override
    public String visitComment(CommentTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~COMMENT: " + node.getBody());
    }

    @Override
    public String visitDeprecated(DeprecatedTree node, Void _v) {
        return ElementUtils.escapeFormat("@deprecated " + node.getBody());
    }

    @Override
    public String visitDocComment(DocCommentTree node, Void _v) {
        // A documentation comment comprised of multiple documentation nodes.

        // The body of the comment.
        String result = node.getFullBody().stream().map((innerNode) -> innerNode.accept(this, null)).reduce("", String::concat);
            
        // The block tags of the comment.
        result += node.getBlockTags().stream().map((innerNode) -> innerNode.accept(this, null)).reduce("", String::concat);

        return result;
    }

    @Override
    public String visitDocRoot(DocRootTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCDOCROOT: " + node.toString());
    }

    @Override
    public String visitEndElement(EndElementTree node, Void _v) {
        // A documentation HTML element, such as </p> or </ul>.
        return ElementUtils.escapeFormat("");
    }

    @Override
    public String visitEntity(EntityTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCENTITY: " + node.getName());
    }

    @Override
    public String visitErroneous(ErroneousTree node, Void _v) {
        // Parser couldn't understand this malformed documentation node.
        // Just print it.
        return ElementUtils.escapeFormat(node.getBody());
    }

    @Override
    public String visitIdentifier(IdentifierTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCIDENTIFIER: " + node.getName());
    }

    @Override
    public String visitInheritDoc(InheritDocTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCINHERITDOC: " + node.toString());
    }

    @Override
    public String visitLink(LinkTree node, Void _v) {
        // In Java, this is {@link link}
        // In Haxe, this is `full.path.to.link`

        return String.format("`%s`", node.getReference().accept(this, null));
    }

    @Override
    public String visitLiteral(LiteralTree node, Void _v) {
        // In Java, this is {@code text}
        // In Haxe, this is `text`

        return ElementUtils.escapeFormat("`" + node.getBody() + "`");
    }

    @Override
    public String visitParam(ParamTree node, Void _v) {
        return ElementUtils.escapeFormat("@param : " + node.getName() + " " + node.getDescription());
    }

    @Override
    public String visitReference(ReferenceTree node, Void _v) {
        // A package.name.class#member reference.

        return node.getSignature();
    }

    @Override
    public String visitReturn(ReturnTree node, Void _v) {
        return ElementUtils.escapeFormat("@return " + node.getDescription());
    }

    @Override
    public String visitSee(SeeTree node, Void _v) {
        return ElementUtils.escapeFormat("@see " + node.getReference());
    }

    @Override
    public String visitSerial(SerialTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCSERIAL: " + node.toString());
    }

    @Override
    public String visitSerialData(SerialDataTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCSERIALDATA: " + node.toString());
    }

    @Override
    public String visitSerialField(SerialFieldTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCSERIALFIELD: " + node.getName() + " " + node.getDescription());
    }

    @Override
    public String visitSince(SinceTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCSINCE: " + node.getBody());
    }

    @Override
    public String visitStartElement(StartElementTree node, Void _v) {
        // A documentation HTML element, such as <p> or <ul>.
        return ElementUtils.escapeFormat("");
    }

    @Override
    public String visitText(TextTree node, Void _v) {
        // A line of documentation that's just a string.
        return ElementUtils.escapeFormat(node.getBody());
    }

    @Override
    public String visitThrows(ThrowsTree node, Void _v) {
        // Throws an exception of the given type under the given circumstances.
        return ElementUtils.escapeFormat("@throws " + node.getExceptionName() + " " + node.getDescription());
    }

    @Override
    public String visitUnknownBlockTag(UnknownBlockTagTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCUNKNOWNBLOCKTAG: " + node.getTagName() + " " + node.getContent());
    }

    @Override
    public String visitUnknownInlineTag(UnknownInlineTagTree node, Void _v) {
        // Try using it as an attribute.
        return ElementUtils.escapeFormat("@" + node.getTagName() + " " + node.getContent());
    }

    @Override
    public String visitValue(ValueTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCVALUE: " + node.toString());
    }

    @Override
    public String visitVersion(VersionTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCVERSION: " + node.getBody());
    }

    @Override
    public String visitOther(DocTree node, Void _v) {
        return ElementUtils.escapeFormat("~~~DOCOTHER: " + node.toString());
    }
}
