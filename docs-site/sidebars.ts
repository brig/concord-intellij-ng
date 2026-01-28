import type { SidebarsConfig } from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  docs: [
    {
      type: 'doc',
      id: 'intro',
      label: 'Intro',
    },

    {
      type: 'doc',
      id: 'install',
      label: 'Install / Download',
    },

    {
      type: 'category',
      label: 'Features',
      collapsed: false,
      link: {
        type: 'doc',
        id: 'features/overview',
      },
      items: [
        'features/completion',
        'features/highlighting',
        'features/language-injection',
        'features/validation',
        'features/inspections',
        'features/quick-fixes',
        'features/navigation',
        'features/find-usages',
        'features/call-hierarchy',
        'features/structure-view',
        'features/rename',
        'features/flow-docs',
        'features/scopes',
      ],
    },
  ],
};

export default sidebars;
