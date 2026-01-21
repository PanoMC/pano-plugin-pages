import {PanoPlugin, viewComponent} from '@panomc/sdk';
import {derived} from 'svelte/store';
import {_ as i18n} from '@panomc/sdk/utils/language';
import ApiUtil from '@panomc/sdk/utils/api';

export const pluginId = 'pano-plugin-pages';

// this is to render plugin translations
export const _ = derived(i18n, ($_fn) => {
  return (key, options) => $_fn(`plugins.${pluginId}.${key}`, options);
});

export default class PagesPlugin extends PanoPlugin {
  async onLoad() {
    const pano = this.pano;

    if (pano.isPanel) {
      // Register Panel Page
      pano.ui.page.register({
        path: '/pages',
        component: viewComponent(() => import('./panel/PagesPage.svelte')),
        permission: `pano.plugin.${pluginId}.manage.pages`,
      });

      // Register Page Editor (Create)
      pano.ui.page.register({
        path: '/pages/create',
        component: viewComponent(() => import('./panel/PageEditor.svelte')),
        permission: `pano.plugin.${pluginId}.manage.pages`,
      });

      // Register Page Editor (Edit)
      pano.ui.page.register({
        path: '/pages/edit/:id',
        component: viewComponent(() => import('./panel/PageEditor.svelte')),
        permission: `pano.plugin.${pluginId}.manage.pages`,
      });

      // Register Panel Navigation
      pano.ui.nav.site.editNavLinks((navigationItems) => {
        const postsIndex = navigationItems.findIndex((item) => item.href === '/posts');
        const statisticsIndex = navigationItems.findIndex((item) => item.href === '/statistics');

        const pagesLink = {
          href: '/pages',
          icon: 'fas fa-file-lines',
          text: `plugins.${pluginId}.pages.nav`,
          startsWith: true,
          permission: `pano.plugin.${pluginId}.manage.pages`,
        };

        if (postsIndex !== -1) {
          // varsa posts'un üstünde
          navigationItems.splice(postsIndex, 0, pagesLink);
        } else if (statisticsIndex !== -1) {
          // yoksa statistics'in altında
          navigationItems.splice(statisticsIndex + 1, 0, pagesLink);
        } else {
          // yoksa sona eklensin
          navigationItems.push(pagesLink);
        }

        return navigationItems;
      });
    } else {
      // Theme Side
      const customPageComponent = viewComponent(() => import('./theme/CustomPage.svelte'));

      pano.ui.app.onLoad(async (data, event) => {
        // Fetch active pages to register their routes
        try {
          const res = await ApiUtil.get({ path: '/api/pages', request: event });
          if (res) {
            const pages = res.pages;

            pages.forEach((page) => {
              // Register dynamic route
              pano.ui.page.register({
                path: page.url,
                component: customPageComponent,
                loginRequired: page.loginRequired,
                permission: page.permissionNode,
                resetLayout: page.resetLayout,
              });

              // If registered to theme navigation
              if (page.registerToThemeNav) {
                // Here we would typically use an API to add to theme nav if the theme supports it
                // For now, we can use editNavLinks if it's available for the theme too
                if (pano.ui.nav.site.editNavLinks) {
                  pano.ui.nav.site.editNavLinks((navItems) => {
                    // Check if already exists
                    if (!navItems.find((n) => n.href === page.url)) {
                      navItems.push({
                        href: page.url,
                        text: page.title, // Literal text
                        startsWith: false,
                        loginRequired: page.loginRequired,
                        permission: page.permissionNode,
                      });
                    }
                    return navItems;
                  });
                }
              }
            });
          }
        } catch (e) {
          console.error('[PagesPlugin] Failed to fetch pages for route registration', e);
        }
      });
    }
  }

  onContextUpdate(ctx) { }

  onUnload() { }
}
