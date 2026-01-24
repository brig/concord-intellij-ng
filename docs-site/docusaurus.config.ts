const config = {
    title: 'Concord IntelliJ Plugin',
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
                    editUrl: 'https://github.com/brig/concord-intellij-ng/tree/main/docs-site/',
                },
                blog: {
                    showReadingTime: true,
                    editUrl: 'https://github.com/brig/concord-intellij-ng/tree/main/docs-site/',
                },
                theme: { customCss: require.resolve('./src/css/custom.css') },
            },
        ],
    ],
};
export default config;
