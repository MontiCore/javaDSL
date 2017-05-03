/*
 * Copyright (c) 2003, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.doclets.formats.html;

import java.io.*;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.markup.*;
import com.sun.tools.doclets.internal.toolkit.*;

/**
 * Writes annotation type optional member documentation in HTML format.
 *
 * @author Jamie Ho
 * @author Bhavesh Patel (Modified)
 */
public class AnnotationTypeOptionalMemberWriterImpl extends
        AnnotationTypeRequiredMemberWriterImpl
    implements AnnotationTypeOptionalMemberWriter, MemberSummaryWriter {

    /**
     * Construct a new AnnotationTypeOptionalMemberWriterImpl.
     *
     * @param writer         the writer that will write the output.
     * @param annotationType the AnnotationType that holds this member.
     */
    public AnnotationTypeOptionalMemberWriterImpl(SubWriterHolderWriter writer,
        AnnotationTypeDoc annotationType) {
        super(writer, annotationType);
    }

    /**
     * {@inheritDoc}
     */
    public Content getMemberSummaryHeader(ClassDoc classDoc,
            Content memberSummaryTree) {
        memberSummaryTree.addContent(
                HtmlConstants.START_OF_ANNOTATION_TYPE_OPTIONAL_MEMBER_SUMMARY);
        Content memberTree = writer.getMemberTreeHeader();
        writer.addSummaryHeader(this, classDoc, memberTree);
        return memberTree;
    }

    /**
     * {@inheritDoc}
     */
    public void addDefaultValueInfo(MemberDoc member, Content annotationDocTree) {
        if (((AnnotationTypeElementDoc) member).defaultValue() != null) {
            Content dt = HtmlTree.DT(writer.getResource("doclet.Default"));
            Content dl = HtmlTree.DL(dt);
            Content dd = HtmlTree.DD(new StringContent(
                    ((AnnotationTypeElementDoc) member).defaultValue().toString()));
            dl.addContent(dd);
            annotationDocTree.addContent(dl);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * {@inheritDoc}
     */
    public void addSummaryLabel(Content memberTree) {
        Content label = HtmlTree.HEADING(HtmlConstants.SUMMARY_HEADING,
                writer.getResource("doclet.Annotation_Type_Optional_Member_Summary"));
        memberTree.addContent(label);
    }

    /**
     * {@inheritDoc}
     */
    public String getTableSummary() {
        return configuration().getText("doclet.Member_Table_Summary",
                configuration().getText("doclet.Annotation_Type_Optional_Member_Summary"),
                configuration().getText("doclet.annotation_type_optional_members"));
    }

    /**
     * {@inheritDoc}
     */
    public String getCaption() {
        return configuration().getText("doclet.Annotation_Type_Optional_Members");
    }

    /**
     * {@inheritDoc}
     */
    public String[] getSummaryTableHeader(ProgramElementDoc member) {
        String[] header = new String[] {
            writer.getModifierTypeHeader(),
            configuration().getText("doclet.0_and_1",
                    configuration().getText("doclet.Annotation_Type_Optional_Member"),
                    configuration().getText("doclet.Description"))
        };
        return header;
    }

    /**
     * {@inheritDoc}
     */
    public void addSummaryAnchor(ClassDoc cd, Content memberTree) {
        memberTree.addContent(writer.getMarkerAnchor(
                "annotation_type_optional_element_summary"));
    }

    /**
     * {@inheritDoc}
     */
    protected Content getNavSummaryLink(ClassDoc cd, boolean link) {
        if (link) {
            return writer.getHyperLink("", "annotation_type_optional_element_summary",
                    writer.getResource("doclet.navAnnotationTypeOptionalMember"));
        } else {
            return writer.getResource("doclet.navAnnotationTypeOptionalMember");
        }
    }
}
