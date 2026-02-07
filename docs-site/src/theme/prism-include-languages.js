module.exports = function prismIncludeLanguages(Prism) {
  if (!Prism.languages.yaml) {
    return;
  }

  // Ensure 'concord' language is defined by extending 'yaml'
  if (!Prism.languages.concord) {
    Prism.languages.concord = Prism.languages.extend('yaml', {});
  }

  Prism.languages.insertBefore('concord', 'key', {
    concordSection: {
      pattern: /\b(flows|configuration|forms|triggers|publicFlows|imports|profiles|resources)\b(?=\s*:)/m,
      alias: ['top-section'],
    },
    concordStep: {
      pattern: /\b(call|log|task|checkpoint|if|then|else|switch|return|throw|set|parallel|script)\b(?=\s*:)/m,
      alias: ['step'],
    },
    dslKey: {
      pattern: /\b(runtime|in|out|meta|method|body|headers|auth|debug|entryPoint|arguments|exclusive|tasks|dependencies)\b(?=\s*:)/m,
      alias: ['dsl-key'],
    },
    expression: {
      pattern: /\$\{[\s\S]*?\}/,
      alias: ['expression'],
    },
    flowName: {
      pattern: /\b(main|default|deployApp|myFlow|anotherFlow|deploy|production)\b(?=\s*:)/,
      alias: 'flow-name',
    },
    targetId: {
      pattern: /\b(processS3|connect|deployApp|setupEnvironment|main|setupFlow|deployProd|buildApp|deployToCluster|deployDev|setupEnv)\b/,
      alias: 'target-id'
    },
    errorForExamples: {
        pattern: /\bstepps\b/,
        alias: 'error',
    },
    warnForExamples: {
      pattern: /\bstrnig\b/,
      alias: ['warn'],
    },
    warnFlowNameForExamples: {
      pattern: /\bduplicateFlowName\b/,
      alias: ['warn', 'flow-name'],
    },
  });

  Prism.languages.insertBefore('concord', 'comment', {
    concordDocComment: {
      pattern: /#.*/,
      inside: {
        paramName: {
          pattern: /\b(userName|appName|result|s3Bucket|s3Prefix|s3Processed|config)\b/,
          alias: 'param-name',
        },
        invalidType: {
          pattern: /\bstrnig\b/,
          alias: ['error', 'param-type'],
        },
        validType: {
          pattern: /\b(string|int|boolean|number|object)\b/,
          alias: 'param-type',
        },
        validType: {
          pattern: /\b(in|out)\b/,
          alias: 'doc-section',
        },
        madnatory: {
          pattern: /\bmandatory\b/,
          alias: 'param-mandatory',
        },
        optional: {
          pattern: /\boptional\b/,
          alias: 'param-optional',
        },
        text: {
          pattern: /\b(param description)\b/,
          alias: 'param-text',
        }
      },
    },
  });
};
