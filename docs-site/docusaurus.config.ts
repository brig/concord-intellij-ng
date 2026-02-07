import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const isDev = process.env.NODE_ENV === 'development' || process.env.INCLUDE_CURRENT_VERSION === 'true';

const config: Config = {
    title: 'Concord IntelliJ Plugin',
    favicon: 'favicon.png',

    url: 'https://brig.github.io',
    baseUrl: '/concord-intellij-ng/',
    organizationName: 'brig',
    projectName: 'concord-intellij-ng',
    trailingSlash: false,

    presets: [
        [
            'classic',
            {
                docs: {
                    sidebarPath: require.resolve('./sidebars.ts'),
                    routeBasePath: 'docs',
                    includeCurrentVersion: isDev,
                    lastVersion: isDev ? 'current' : undefined,
                    versions: isDev ? {
                        current: {
                            label: 'Unreleased'
                        },
                    } : {}
                },
                blog: {
                    showReadingTime: true
                },
                theme: { customCss: require.resolve('./src/css/custom.css') },
            } satisfies Preset.Options,
        ],
    ],

    themeConfig: {
        colorMode: {
            defaultMode: 'dark',
            disableSwitch: true,
            respectPrefersColorScheme: false,
        },
        navbar: {
            title: 'Concord IntelliJ Plugin',
            logo: {
                alt: 'Concord IntelliJ Plugin',
                src: 'img/logo.png',
            },
            items: [
                {
                    type: 'docsVersionDropdown',
                    position: 'left',
                    className: 'navbarVersion',
                    dropdownActiveClassDisabled: true
                },

                {
                    to: '/blog',
                    label: 'What’s new',
                    position: 'left',
                },
                {
                    href: 'https://github.com/brig/concord-intellij-ng',
                    label: 'GitHub',
                    position: 'right',
                },
            ],
        },
        prism: {
            additionalLanguages: ['yaml', 'concord'],
        },
    },
};
export default config;
