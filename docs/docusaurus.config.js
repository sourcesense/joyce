/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'Joyce',
  tagline: 'Joyce is a highly scalable event-driven Cloud Native Data Hub.',
  url: 'https://sourcesense.github.io',
  baseUrl: '/joyce/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'throw',
  favicon: 'img/favicon.ico',
  organizationName: 'sourcesense', // Usually your GitHub org/user name.
  projectName: 'joyce', // Usually your repo name.
  themeConfig: {
    googleAnalytics: {
      trackingID: 'UA-56700273-2',
      // Optional fields.
      anonymizeIP: true, // Should IPs be anonymized?
    },
    navbar: {
      title: 'Joyce',
      logo: {
        alt: 'Joyce',
        src: 'img/logo-art-black.png',
      },
      items: [
        {
          type: 'doc',
          docId: 'overview',
          position: 'left',
          label: 'Documentation',
        },
        {to: '/getting-started', label: 'Getting Started', position: 'left'},
        {
          href: 'https://github.com/sourcesense/joyce',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Community',
          items: [
            {
              label: 'Gitter',
              href: 'https://gitter.im/sourcesense/joyce',
            }
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'Sourcesense',
              to: 'https://sourcesense.com',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/sourcesense/joyce',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Sourcesense Spa. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl:
            'https://github.com/sourcesense/joyce/edit/main/docs',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
