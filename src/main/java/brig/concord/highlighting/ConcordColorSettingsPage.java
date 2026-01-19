package brig.concord.highlighting;

import brig.concord.ConcordIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class ConcordColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Sections//Top-level sections (flows, configuration, ...)",
                    ConcordHighlightingColors.DSL_SECTION),

            new AttributesDescriptor("Flows//Flow name",
                    ConcordHighlightingColors.FLOW_IDENTIFIER),

            new AttributesDescriptor("Steps//Step keyword (task, call, log, ...)",
                    ConcordHighlightingColors.STEP_KEYWORD),
            new AttributesDescriptor("Steps//Target identifier (task/call value)",
                    ConcordHighlightingColors.TARGET_IDENTIFIER),
            new AttributesDescriptor("Steps//Label (name)",
                    ConcordHighlightingColors.DSL_LABEL),

            new AttributesDescriptor("Imports & Triggers//Kind (git, mvn, github, cron, ...)",
                    ConcordHighlightingColors.DSL_KIND),
            new AttributesDescriptor("DSL//Predefined keys (in/out/retry/url/entryPoint/...)",
                    ConcordHighlightingColors.DSL_KEY),
            new AttributesDescriptor("DSL//User keys",
                    ConcordHighlightingColors.USER_KEY),

            new AttributesDescriptor("Values//Expression ${...}",
                    ConcordHighlightingColors.EXPRESSION),
            new AttributesDescriptor("Values//String",
                    ConcordHighlightingColors.STRING),
            new AttributesDescriptor("Values//Number",
                    ConcordHighlightingColors.NUMBER),
            new AttributesDescriptor("Values//Boolean",
                    ConcordHighlightingColors.BOOLEAN),
            new AttributesDescriptor("Values//Null",
                    ConcordHighlightingColors.NULL),

            new AttributesDescriptor("YAML//Comment",
                    ConcordHighlightingColors.COMMENT),
            new AttributesDescriptor("YAML//Colon",
                    ConcordHighlightingColors.COLON),
            new AttributesDescriptor("YAML//Brackets / braces",
                    ConcordHighlightingColors.BRACKETS),

            new AttributesDescriptor("Flow documentation//Marker (##)",
                    ConcordHighlightingColors.FLOW_DOC_MARKER),
            new AttributesDescriptor("Flow documentation//Comment prefix (#)",
                    ConcordHighlightingColors.FLOW_DOC_COMMENT_PREFIX),
            new AttributesDescriptor("Flow documentation//Section (in:/out:)",
                    ConcordHighlightingColors.FLOW_DOC_SECTION),
            new AttributesDescriptor("Flow documentation//Parameter name",
                    ConcordHighlightingColors.FLOW_DOC_PARAM_NAME),
            new AttributesDescriptor("Flow documentation//Type",
                    ConcordHighlightingColors.FLOW_DOC_TYPE),
            new AttributesDescriptor("Flow documentation//Mandatory",
                    ConcordHighlightingColors.FLOW_DOC_MANDATORY),
            new AttributesDescriptor("Flow documentation//Optional",
                    ConcordHighlightingColors.FLOW_DOC_OPTIONAL),
            new AttributesDescriptor("Flow documentation//Description text",
                    ConcordHighlightingColors.FLOW_DOC_TEXT),
            new AttributesDescriptor("Flow documentation//Punctuation (: ,)",
                    ConcordHighlightingColors.FLOW_DOC_PUNCTUATION),

            new AttributesDescriptor("Bad character",
                    ConcordHighlightingColors.BAD_CHARACTER),
    };

    /**
     * Demo text for color settings preview.
     * Tags are replaced with highlighting and not shown to user.
     */
    @NonNls
    private static final String DEMO_TEXT = """
<comment># Concord Workflow Definition</comment>
<comment># Demonstrates all highlightable elements</comment>

<section>configuration</section>:
  <dslkey>runtime</dslkey>: <string>"concord-v2"</string>
  <dslkey>entryPoint</dslkey>: <string>"main"</string>
  <dslkey>debug</dslkey>: <bool>true</bool>
  <dslkey>processTimeout</dslkey>: <string>"PT1H"</string>
  <dslkey>arguments</dslkey>:
    <userkey>apiUrl</userkey>: <string>"https://api.example.com"</string>
    <userkey>maxRetries</userkey>: <num>3</num>
    <userkey>enabled</userkey>: <bool>true</bool>
    <userkey>timeout</userkey>: <null>null</null>
  <dslkey>dependencies</dslkey>:
    - <string>"mvn://com.example:tasks:1.0.0"</string>

<section>publicFlows</section>:
  - <string>"main"</string>
  - <string>"deploy"</string>

<section>flows</section>:
  <fdmarker>##</fdmarker>
  <fdprefix>#</fdprefix> <fdtext>Process data and return results</fdtext>
  <fdprefix>#</fdprefix> <fdsection>in:</fdsection>
  <fdprefix>#</fdprefix>   <fdparam>inputData</fdparam><fdpunct>:</fdpunct> <fdtype>object</fdtype><fdpunct>,</fdpunct> <fdmandatory>mandatory</fdmandatory><fdpunct>,</fdpunct> <fdtext>Input data to process</fdtext>
  <fdprefix>#</fdprefix>   <fdparam>maxItems</fdparam><fdpunct>:</fdpunct> <fdtype>int</fdtype><fdpunct>,</fdpunct> <fdoptional>optional</fdoptional><fdpunct>,</fdpunct> <fdtext>Maximum items to process</fdtext>
  <fdprefix>#</fdprefix> <fdsection>out:</fdsection>
  <fdprefix>#</fdprefix>   <fdparam>result</fdparam><fdpunct>:</fdpunct> <fdtype>object</fdtype><fdpunct>,</fdpunct> <fdtext>Processing result</fdtext>
  <fdmarker>##</fdmarker>
  <flowname>main</flowname>:
    <comment># Task call with full configuration</comment>
    - <label>name</label>: <string>"Fetch API Data"</string>
      <step>task</step>: <target>"http"</target>
      <dslkey>in</dslkey>:
        <userkey>url</userkey>: <expr>${apiUrl}</expr>
        <userkey>method</userkey>: <string>"GET"</string>
        <userkey>headers</userkey>:
          <userkey>Authorization</userkey>: <expr>${authToken}</expr>
      <dslkey>out</dslkey>: <userkey>response</userkey>
      <dslkey>retry</dslkey>:
        <dslkey>times</dslkey>: <expr>${maxRetries}</expr>
        <dslkey>delay</dslkey>: <num>5</num>
      <dslkey>meta</dslkey>:
        <userkey>segmentName</userkey>: <string>"API Request"</string>
      <dslkey>ignoreErrors</dslkey>: <bool>false</bool>

    <comment># Conditional logic with control flow</comment>
    - <step>if</step>: <expr>${response.ok}</expr>
      <step>then</step>:
        - <step>log</step>: <string>"Request succeeded!"</string>
        - <step>set</step>:
            <userkey>result</userkey>: <expr>${response.content}</expr>
      <step>else</step>:
        - <step>throw</step>: <expr>${response.error}</expr>

    <comment># Switch statement</comment>
    - <step>switch</step>: <expr>${env}</expr>
      <userkey>prod</userkey>:
        - <step>call</step>: <target>"deployProd"</target>
      <userkey>staging</userkey>:
        - <step>call</step>: <target>"deployStaging"</target>
      <step>default</step>:
        - <step>log</step>: <string>"Unknown environment"</string>

    <comment># Parallel execution</comment>
    - <step>parallel</step>:
        - <step>task</step>: <target>"slack"</target>
          <dslkey>in</dslkey>:
            <userkey>channelId</userkey>: <string>"C123"</string>
            <userkey>text</userkey>: <string>"Deployment complete!"</string>
        - <step>task</step>: <target>"email"</target>
          <dslkey>in</dslkey>:
            <userkey>to</userkey>: <string>"team@example.com"</string>
            <userkey>subject</userkey>: <string>"Deploy Notification"</string>
      <dslkey>out</dslkey>:
        - <userkey>slackResult</userkey>
        - <userkey>emailResult</userkey>

    <comment># Try-catch block</comment>
    - <step>try</step>:
        - <step>call</step>: <target>"riskyOperation"</target>
      <step>error</step>:
        - <step>log</step>: <expr>${lastError}</expr>
        - <step>suspend</step>: <string>"manual-review"</string>

    <comment># Call with loop</comment>
    - <step>call</step>: <target>"processItem"</target>
      <dslkey>in</dslkey>:
        <userkey>item</userkey>: <expr>${item}</expr>
      <dslkey>loop</dslkey>:
        <dslkey>items</dslkey>: <expr>${itemList}</expr>
        <dslkey>mode</dslkey>: <string>"parallel"</string>
        <dslkey>parallelism</dslkey>: <num>5</num>

    - <step>checkpoint</step>: <string>"afterProcessing"</string>
    - <step>return</step>

  <flowname>processItem</flowname>:
    - <step>log</step>: <expr>${item.name}</expr>
    - <step>expr</step>: <expr>${item.process()}</expr>
      <dslkey>out</dslkey>: <userkey>processResult</userkey>

<section>forms</section>:
  <flowname>approvalForm</flowname>:
    - <userkey>approver</userkey>: { <userkey>label</userkey>: <string>"Approver"</string>, <userkey>type</userkey>: <string>"string"</string> }
    - <userkey>approved</userkey>: { <userkey>label</userkey>: <string>"Approved?"</string>, <userkey>type</userkey>: <string>"boolean"</string> }
    - <userkey>comments</userkey>: { <userkey>label</userkey>: <string>"Comments"</string>, <userkey>type</userkey>: <string>"string?"</string> }

<section>triggers</section>:
  - <kind>manual</kind>:
      <dslkey>name</dslkey>: <label>"Manual Run"</label>
      <dslkey>entryPoint</dslkey>: <string>"main"</string>
      <dslkey>arguments</dslkey>:
        <userkey>env</userkey>: <string>"dev"</string>
  - <kind>cron</kind>:
      <dslkey>spec</dslkey>: <string>"0 9 * * MON"</string>
      <dslkey>entryPoint</dslkey>: <string>"main"</string>
      <dslkey>timezone</dslkey>: <string>"America/New_York"</string>
  - <kind>github</kind>:
      <dslkey>version</dslkey>: <num>2</num>
      <dslkey>entryPoint</dslkey>: <string>"onPush"</string>
      <dslkey>conditions</dslkey>:
        <userkey>type</userkey>: <string>"push"</string>
        <userkey>branch</userkey>: <string>"main"</string>

<section>imports</section>:
  - <kind>git</kind>:
      <dslkey>url</dslkey>: <string>"https://github.com/example/tasks.git"</string>
      <dslkey>version</dslkey>: <string>"v1.0.0"</string>
      <dslkey>path</dslkey>: <string>"flows"</string>
      <dslkey>dest</dslkey>: <string>"imported"</string>
  - <kind>mvn</kind>:
      <dslkey>url</dslkey>: <string>"mvn://com.example:concord-tasks:2.0.0"</string>

<section>profiles</section>:
  <flowname>production</flowname>:
    <section>configuration</section>:
      <dslkey>arguments</dslkey>:
        <userkey>env</userkey>: <string>"prod"</string>
        <userkey>debug</userkey>: <bool>false</bool>
""";

    /**
     * Maps XML-like tags in demo text to TextAttributesKey.
     */
    @NonNls
    private static final Map<String, TextAttributesKey> ADDITIONAL_TAGS = Map.ofEntries(
            Map.entry("section", ConcordHighlightingColors.DSL_SECTION),

            Map.entry("flowname", ConcordHighlightingColors.FLOW_IDENTIFIER),
            Map.entry("step", ConcordHighlightingColors.STEP_KEYWORD),
            Map.entry("target", ConcordHighlightingColors.TARGET_IDENTIFIER),

            Map.entry("dslkey", ConcordHighlightingColors.DSL_KEY),
            Map.entry("label", ConcordHighlightingColors.DSL_LABEL),
            Map.entry("kind", ConcordHighlightingColors.DSL_KIND),

            Map.entry("userkey", ConcordHighlightingColors.USER_KEY),

            Map.entry("expr", ConcordHighlightingColors.EXPRESSION),
            Map.entry("string", ConcordHighlightingColors.STRING),
            Map.entry("num", ConcordHighlightingColors.NUMBER),
            Map.entry("bool", ConcordHighlightingColors.BOOLEAN),
            Map.entry("null", ConcordHighlightingColors.NULL),

            Map.entry("comment", ConcordHighlightingColors.COMMENT),

            Map.entry("fdmarker", ConcordHighlightingColors.FLOW_DOC_MARKER),
            Map.entry("fdprefix", ConcordHighlightingColors.FLOW_DOC_COMMENT_PREFIX),
            Map.entry("fdsection", ConcordHighlightingColors.FLOW_DOC_SECTION),
            Map.entry("fdparam", ConcordHighlightingColors.FLOW_DOC_PARAM_NAME),
            Map.entry("fdtype", ConcordHighlightingColors.FLOW_DOC_TYPE),
            Map.entry("fdmandatory", ConcordHighlightingColors.FLOW_DOC_MANDATORY),
            Map.entry("fdoptional", ConcordHighlightingColors.FLOW_DOC_OPTIONAL),
            Map.entry("fdtext", ConcordHighlightingColors.FLOW_DOC_TEXT),
            Map.entry("fdpunct", ConcordHighlightingColors.FLOW_DOC_PUNCTUATION)
    );

    @Nullable
    @Override
    public Icon getIcon() {
        return ConcordIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new ConcordSyntaxHighlighter();
    }

    @NotNull
    @Override
    public @NonNls String getDemoText() {
        return DEMO_TEXT;
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ADDITIONAL_TAGS;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Concord";
    }
}
