<?xml version="1.0"?>

<!--
    Basic initial XSL stylesheet for formatting intact objects to display.
    This file will be modified to provide better display options as the project
    progresses.

    author: Chris Lewington, Sugath Mudali November, December 2002
    version: $Id$
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns="http://www.w3.org/TR/REC-html40">
    <!--
        Defaults to zero if none was specified. Note the single quotes
        around the value. -->
    <xsl:param name="tableName" select="'tbl_0'"/>
    <xsl:param name="searchLink"/>

    <xsl:output method="xml" version="1.0" indent="yes"
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
    <!--
    ****************************************************************************
    ** Top level template. Creates the framework for the XHTML page.
    *************************************************************************-->
    <xsl:template match="/">

        <!-- Styles for various rows; change them to suit you. -->
        <style>
            <xsl:comment>
                tr.Experiment {background-color: rgb(255, 255, 102)}
                tr.Interaction {background-color: rgb(255, 255, 204)}
                tr.Protein {background-color: rgb(255, 255, 51)}
            </xsl:comment>
        </style>

        <table cellpadding="1" cellspacing="0" border="0" width="100%">
            <xsl:call-template name="draw-table-headings"/>
            <xsl:apply-templates/>
        </table>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Template for experiment attribute.
    *************************************************************************-->
    <xsl:template match="Experiment">
        <tr class="Experiment">
            <td colspan="2">
                <b>Experiment</b>
            </td>
            <td>
                <xsl:value-of select="@ac"/>
            </td>
            <td>
                <xsl:value-of select="@shortLabel"/>
            </td>

            <td>
                <xsl:value-of select="@fullName"/>
            </td>
        </tr>

        <xsl:if test="CvIdentification/@shortLabel | CvInteraction/@shortLabel | bioSource">
            <tr class="Experiment">
                <td colspan="2">
                    <xsl:value-of select="CvInteraction/@shortLabel"/>
                </td>
                <td>
                    <xsl:value-of select="CvIdentification/@shortLabel"/>
                </td>
                <td colspan="2">
                    <xsl:value-of select="BioSource/@shortLabel"/>
                </td>
            </tr>
        </xsl:if>

        <xsl:for-each select="annotations/Annotation">
            <tr class="Experiment">
                <td colspan="2">
                    <xsl:value-of select="CvTopic/@shortLabel"/>
                </td>

                <td colspan="3">
                    <xsl:value-of select="@annotationText"/>
                </td>
            </tr>
        </xsl:for-each>

        <xsl:for-each select="xrefs/Xref">
            <tr class="Experiment">
                <xsl:call-template name="draw-xrefs"/>
            </tr>
        </xsl:for-each>

        <xsl:if test="interactions/Interaction">
            <xsl:apply-templates select="interactions/Interaction"/>
        </xsl:if>
    </xsl:template>

    <!-- =================================================================== -->

    <!--
    ****************************************************************************
    ** Template for interaction attribute.
    *************************************************************************-->
    <xsl:template name="Interaction" match="Experiment/interactions/Interaction">

        <tr class="Interaction">
            <xsl:call-template name="draw-checkbox"/>
            <td><b>Interaction</b></td>
            <xsl:call-template name="draw_link">
                <xsl:with-param name="ac" select="@ac"/>
                <xsl:with-param name="type" select="'Interaction'"/>
            </xsl:call-template>

            <td>
                <xsl:value-of select="@shortLabel"/>
            </td>
            <td>
                <xsl:value-of select="@fullName"/>
            </td>
        </tr>

        <xsl:if test="@status='expand'">
            <xsl:if test="CvInteractionType/@shortLabel">
                <tr class="Interaction">
                    <td colspan="2">
                        <xsl:value-of select="CvInteractionType/@shortLabel"/>
                    </td>
                    <td colspan="3">
                        <xsl:value-of select="Float/@value"/>
                    </td>
                </tr>
            </xsl:if>

            <xsl:for-each select="annotations/Annotation">
                <tr class="Interaction">
                    <td colspan="2">
                        <xsl:value-of select="CvTopic/@shortLabel"/>
                    </td>
                    <td colspan="3">
                        <xsl:value-of select="@annotationText"/>
                    </td>
                </tr>
            </xsl:for-each>

            <xsl:for-each select="xrefs/Xref">
                <tr class="Interaction">
                    <xsl:call-template name="draw-xrefs"/>
                </tr>
            </xsl:for-each>
        </xsl:if>

        <!-- compact view - only show ShortLabel/role etc -->
        <xsl:if test="components/Component/Protein/@status='expand'">
            <xsl:apply-templates select="components/Component/Protein"/>
        </xsl:if>
        <xsl:if test="components/Component/Protein/@status='compact'">
            <tr class="Protein">
                <td colspan="5">
                    <xsl:for-each select="components/Component/Protein">
                        <xsl:call-template name="draw-checkbox-no-td"/>
                        <xsl:variable name="title">
                            <xsl:value-of select="../CvComponentRole/@shortLabel"/>
                        </xsl:variable>
                        <xsl:value-of select="@shortLabel"/>
                        <acronym title="{$title}">[<xsl:value-of select="substring(../CvComponentRole/@shortLabel, 1, 1)"/>]</acronym>
                        <!-- Avoid printing  ',' for the last protein -->
                        <xsl:if test="not(position()=last())">, </xsl:if>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>

    </xsl:template>

    <!--
    ****************************************************************************
    ** Template for interactor attribute.
    *************************************************************************-->
    <xsl:template match="components/Component/Protein">

        <tr class="Protein">
            <xsl:call-template name="draw-checkbox"/>
            <td>
                <b>Protein</b>
            </td>
            <td>
                <xsl:value-of select="@ac"/>
            </td>
            <td>
                <xsl:value-of select="@shortLabel"/>[
                <xsl:value-of select="substring(../CvComponentRole/@shortLabel, 1, 1)"/>]
            </td>

            <td colspan="2">
                <xsl:value-of select="@fullName"/>
            </td>
        </tr>

        <xsl:if test="../CvComponentRole">
            <tr class="Protein">
                <td colspan="2">
                    <xsl:value-of select="../CvComponentRole/@shortLabel"/>
                </td>
                <td>
                    <xsl:value-of select="../@stoichiometry"/>
                </td>
                <td colspan="2">
                    <xsl:value-of select="../BioSource/@shortLabel"/>
                </td>
            </tr>
        </xsl:if>

        <xsl:for-each select="annotations/Annotation">
            <tr class="Protein">
                <td colspan="2">
                    <xsl:value-of select="CvTopic/@shortLabel"/>
                </td>
                <td colspan="3">
                    <xsl:value-of select="@annotationText"/>
                </td>
            </tr>
        </xsl:for-each>

        <xsl:for-each select="xrefs/Xref">
            <tr class="Protein">
                <xsl:call-template name="draw-xrefs"/>
            </tr>
        </xsl:for-each>

    </xsl:template>

    <!--
    ****************************************************************************
    ** Template for AC attribute.
    *************************************************************************-->
    <xsl:template name="draw_link">
        <xsl:param name="ac"/>
        <xsl:param name="type"/>
        <xsl:variable name="link">
            <xsl:value-of select="concat($searchLink, $ac, '&amp;', 'searchClass=', $type)"/>
        </xsl:variable>
        <td>
            <a href="{$link}"><xsl:value-of select="$ac"/></a>
        </td>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Template for short label attribute.
    *************************************************************************-->
    <xsl:template match="@shortLabel">
        <td>
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Template to draw a checkbox.
    *************************************************************************-->
    <xsl:template name="draw-checkbox">
        <!-- Checkbox name; table name + _ + ac -->
        <xsl:variable name="cbName">
            <xsl:variable name="ac">
                <xsl:value-of select="@ac"/>
            </xsl:variable>
            <xsl:value-of select="concat($tableName, '_', $ac)"/>
        </xsl:variable>
        <td>
            <input name="{$cbName}" type="checkbox"></input>
        </td>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Template to draw a checkbox.
    *************************************************************************-->
    <xsl:template name="draw-checkbox-no-td">
        <!-- Checkbox name; table name + _ + ac -->
        <xsl:variable name="cbName">
            <xsl:variable name="ac">
                <xsl:value-of select="@ac"/>
            </xsl:variable>
            <xsl:value-of select="concat($tableName, '_X', $ac)"/>
        </xsl:variable>
        <input name="{$cbName}" type="checkbox"></input>
    </xsl:template>
    <!--
    ****************************************************************************
    ** Draw Xreferences.
    ** emptyCells - the number of empty cells to print.
    ** colSpan - the number columns to span for the short label of CvDatabase.
    *************************************************************************-->
    <xsl:template name="draw-table-headings">
        <tr>
            <td width="2%"></td>
            <td width="10%"></td>
            <td width="10%"></td>
            <td width="10%"></td>
            <td width="68%"></td>
        </tr>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Draw the button table.
    *************************************************************************-->
    <xsl:template name="draw-table-buttons">
        <table cellpadding="1" cellspacing="0" border="1" width="100%">
            <tr>
                <td align="center">
                    <input type="submit" Name="submit" value="Submit"/>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Draws two empty cells.
    *************************************************************************-->
    <xsl:template name="draw-1-empty-cell">
        <xsl:call-template name="draw-empty-cells">
            <xsl:with-param name="testValue" select="1"/>
        </xsl:call-template>
    </xsl:template>
    <!--
    ****************************************************************************
    ** Draws two empty cells.
    *************************************************************************-->
    <xsl:template name="draw-2-empty-cells">
        <xsl:call-template name="draw-empty-cells">
            <xsl:with-param name="testValue" select="2"/>
        </xsl:call-template>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Draws three empty cells.
    *************************************************************************-->
    <xsl:template name="draw-3-empty-cells">
        <xsl:call-template name="draw-empty-cells">
            <xsl:with-param name="testValue" select="3"/>
        </xsl:call-template>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Draws four empty cells.
    *************************************************************************-->
    <xsl:template name="draw-4-empty-cells">
        <xsl:call-template name="draw-empty-cells">
            <xsl:with-param name="testValue" select="4"/>
        </xsl:call-template>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Draws an empty cell.
    ** i - the starting value; defaults to 0.
    ** testValue - the value to test after each iteration; will iterate
    **             till i == testValue.
    *************************************************************************-->
    <xsl:template name="draw-empty-cells">
        <xsl:param name="i" select="0"/>
        <xsl:param name="testValue"/>

        <!-- Condition variable for the loop control. -->
        <xsl:variable name="testPassed">
            <!-- Check the condition of the loop. -->
            <xsl:choose>
                <xsl:when test="$i &lt; $testValue">
                    <xsl:text>true</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>false</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="$testPassed='true'">
            <!-- Body of the loop -->
            <td></td>

            <!-- Increment the loop counter and call it again. -->
            <xsl:call-template name="draw-empty-cells">
                <xsl:with-param name="i" select="$i + 1"/>
                <xsl:with-param name="testValue" select="$testValue"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!--
    ****************************************************************************
    ** Draw Xreferences.
    *************************************************************************-->
    <xsl:template name="draw-xrefs">
        <td colspan="2">
            <xsl:value-of select="CvDatabase/@shortLabel"/>
        </td>
        <td>
            <xsl:value-of select="@primaryId"/>
        </td>
        <td>
            <xsl:value-of select="@secondaryId"/>
        </td>
        <td>
            <xsl:value-of select="@dbRelease"/>
        </td>
    </xsl:template>

</xsl:stylesheet>
