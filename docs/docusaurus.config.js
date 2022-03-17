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
        { to: '/getting-started', label: 'Getting Started', position: 'left' },
        {
          type: 'docsVersionDropdown',
          position: 'right',
          dropdownItemsAfter: [{to: '/versions', label: 'All versions'}],
          dropdownActiveClassDisabled: true,
        },
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
            },
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
          editUrl: 'https://github.com/sourcesense/joyce/edit/main/docs',
          includeCurrentVersion: true,
          lastVersion: 'current',
          versions: {
            current: {
              label: 'v1.5',
              path: 'docs',
            },
          },
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
        googleAnalytics: {
          trackingID: 'UA-56700273-2',
          // Optional fields.
          anonymizeIP: true, // Should IPs be anonymized?
        },
      },
    ],
  ],
};
