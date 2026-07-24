<article class="container vstack gap-3">
  <PageActions middleClass="d-none d-lg-flex">
    <div slot="left">
      <a href="{base}/pages" class="btn btn-link" role="button">
        <i class="fas fa-arrow-left"></i>
        <span class="d-lg-inline d-none ms-2"> {$_('pages.editor.back')}</span>
      </a>
    </div>

    <div slot="right">
      <button
        type="button"
        class="btn btn-secondary"
        class:disabled={loading || !isFormValid || (mode === 'edit' && !isChanged)}
        disabled={loading || !isFormValid || (mode === 'edit' && !isChanged)}
        on:click={onSavePage}>
        {$_('pages.editor.save')}
      </button>
    </div>
  </PageActions>

  <div class="card">
    <div class="card-body">
      <div class="row">
        <div class="col-md-6 border-end">
          <!-- Title -->
          <div class="input-group mb-3">
            {#if mode === 'edit'}
              <span class="input-group-text">#{pageData.id}</span>
            {/if}
            <div class="form-floating flex-grow-1">
              <input
                type="text"
                class="form-control form-control-lg"
                id="title"
                bind:value={pageData.title}
                on:input={onTitleInput}
                placeholder={$_('pages.editor.fields.title')}
                required />
              <label for="title">{$_('pages.editor.fields.title')}</label>
            </div>
          </div>

          <!-- Link Name -->
          <div class="input-group mb-3">
            <div class="form-floating flex-grow-1">
              <input
                type="text"
                class="form-control"
                id="linkName"
                bind:value={pageData.linkName}
                on:input={() => (isLinkNameManuallyEdited = true)}
                placeholder={$_('pages.editor.fields.link-name')} />
              <label for="linkName">{$_('pages.editor.fields.link-name')}</label>
            </div>
            <span class="input-group-text">
              <i class="fas fa-info-circle" use:tooltip={[$_('pages.editor.tooltips.link-name')]}
              ></i>
            </span>
          </div>

          <!-- URL Path -->
          <div class="form-floating">
            <input
              type="text"
              class="form-control"
              class:is-invalid={urlError}
              id="url"
              bind:value={pageData.url}
              on:input={() => (isUrlManuallyEdited = true)}
              placeholder={$_('pages.editor.fields.url')}
              required
              title={$_('pages.editor.tooltips.url')} />
            <label for="url">{$_('pages.editor.fields.url')}</label>
          </div>
        </div>

        <div class="col-md-6">
          <!-- Status -->
          <div class="form-check form-switch">
            <input
              class="form-check-input"
              type="checkbox"
              id="pageActive"
              role="switch"
              bind:checked={pageData.active} />
            <label class="form-check-label" for="pageActive">
              {pageData.active ? $_('pages.list.status.active') : $_('pages.list.status.passive')}
            </label>
          </div>

          <!-- Configuration -->
          <div class="vstack gap-2">
            <div class="form-check form-switch">
              <input
                class="form-check-input"
                type="checkbox"
                role="switch"
                id="registerToNav"
                bind:checked={pageData.registerToThemeNav} />
              <label class="form-check-label" for="registerToNav">
                {$_('pages.editor.fields.register-to-nav')}
              </label>
            </div>

            <div class="form-check form-switch">
              <input
                class="form-check-input"
                type="checkbox"
                role="switch"
                id="pageLoginRequired"
                bind:checked={pageData.loginRequired} />
              <label class="form-check-label" for="pageLoginRequired">
                {$_('pages.editor.fields.login-required')}
              </label>
            </div>

            <div class="form-floating">
              <select class="form-select" id="linkTarget" bind:value={pageData.target}>
                <option value="_self">{$_('pages.editor.fields.target-options.normal')}</option>
                <option value="_blank">{$_('pages.editor.fields.target-options.external')}</option>
              </select>
              <label for="linkTarget">{$_('pages.editor.fields.target')}</label>
            </div>

            <div class="form-check form-switch">
              <input
                class="form-check-input"
                type="checkbox"
                role="switch"
                id="resetLayout"
                bind:checked={pageData.resetLayout}
                title={$_('pages.editor.tooltips.reset-layout')} />
              <label class="form-check-label" for="resetLayout">
                {$_('pages.editor.fields.reset-layout')}
                <i class="fas fa-question-circle ms-1 opacity-50"></i>
              </label>
            </div>

            <div class="form-check form-switch">
              <input
                class="form-check-input"
                type="checkbox"
                role="switch"
                id="showBreadcrumb"
                bind:checked={pageData.showBreadcrumb} />
              <label class="form-check-label" for="showBreadcrumb">
                {$_('pages.editor.fields.show-breadcrumb')}
              </label>
            </div>

            <div class="form-floating">
              <input
                type="text"
                class="form-control"
                id="permissionNode"
                bind:value={pageData.permissionNode}
                placeholder="my.custom.permission" />
              <label for="permissionNode">{$_('pages.editor.fields.permission-node')}</label>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="card flex-grow-1">
    <div class="card-body d-flex flex-column">
      <label for="editor" class="form-label">{$_('pages.editor.fields.html-content')}</label>
      <!-- Editor -->
      <div id="editor">
        <Editor
          bind:content={pageData.htmlContent}
          bind:isEmpty={isEditorEmpty}
          showHtml={true}
          showPreview={true}
          contentStyles="min-height: 400px;" />
      </div>
    </div>
  </div>
</article>

<script context="module">
  import ApiUtil from '@panomc/sdk/utils/api';

  import { pluginId } from '../main';

  export async function load(event) {
    const { params, parent } = event;
    const { pageTitle } = await parent();

    if (params.id) {
      const id = params.id;
      pageTitle.set(`plugins.${pluginId}.pages.editor.edit-title`);

      const body = await ApiUtil.get({
        path: `/api/panel/pages/${id}`,
        request: event,
      });

      if (body.error) {
        return { data: { pageData: {}, mode: 'error', error: body.error } };
      }
      return { data: { pageData: body.page, mode: 'edit' } };
    } else {
      pageTitle.set(`plugins.${pluginId}.pages.editor.create-title`);
      return {
        data: {
          pageData: {
            id: null,
            title: '',
            linkName: '',
            url: '',
            htmlContent: '',
            active: true,
            loginRequired: false,
            permissionNode: '',
            target: '_self',
            resetLayout: false,
            showBreadcrumb: true,
            registerToThemeNav: true,
          },
          mode: 'create',
        },
      };
    }
  }
</script>

<script>
  import { Editor, PageActions } from '@panomc/sdk/components/panel';
  import { showToast } from '@panomc/sdk/toasts';
  import { base, goto } from '@panomc/sdk/svelte';
  import { _ } from '../main';
  import tooltip from '@panomc/sdk/utils/tooltip';

  export let data;

  let loading = false;
  let isEditorEmpty = true;
  let pageData = data.pageData;
  let mode = data.mode;
  let urlError = false;

  let initialPageData = JSON.parse(JSON.stringify(pageData));

  $: {
    if (data.mode !== mode || (data.pageData && data.pageData.id !== pageData.id)) {
      mode = data.mode;
      pageData = data.pageData;
      initialPageData = JSON.parse(JSON.stringify(pageData));
      urlError = false;
      isLinkNameManuallyEdited = false;
      isUrlManuallyEdited = false;
    }
  }

  let isLinkNameManuallyEdited = false;
  let isUrlManuallyEdited = false;

  function onTitleInput() {
    if (mode === 'create') {
      if (!isLinkNameManuallyEdited) {
        pageData.linkName = pageData.title;
      }

      if (!isUrlManuallyEdited) {
        pageData.url = '/' + slugify(pageData.title);
      }
    }
  }

  function slugify(text) {
    if (!text) return '';
    const charMap = {
      ğ: 'g',
      Ğ: 'g',
      ş: 's',
      Ş: 's',
      ı: 'i',
      İ: 'i',
      ö: 'o',
      Ö: 'o',
      ç: 'c',
      Ç: 'c',
      ü: 'u',
      Ü: 'u',
    };
    let str = text.toLowerCase();
    for (const key in charMap) {
      str = str.replace(new RegExp(key, 'g'), charMap[key]);
    }
    return str
      .replace(/[^a-z0-9 -]/g, '') // remove invalid chars
      .replace(/\s+/g, '-') // collapse whitespace and replace by -
      .replace(/-+/g, '-') // collapse dashes
      .replace(/^-+|-+$/g, ''); // trim dashes
  }

  $: isFormValid = (() => {
    if (!pageData.title || pageData.title.trim() === '') return false;
    if (!pageData.url || pageData.url.trim() === '') return false;
    if (isEditorEmpty) return false;
    return true;
  })();

  $: isChanged = mode === 'create' || JSON.stringify(pageData) !== JSON.stringify(initialPageData);

  function goBack() {
    goto(`${base}/pages`);
  }

  async function onSavePage() {
    urlError = false;

    if (pageData.url && !pageData.url.startsWith('/') && !/^https?:\/\//.test(pageData.url)) {
      pageData.url = '/' + pageData.url;
    }

    if (!isFormValid) return;

    loading = true;

    try {
      const result = await ApiUtil[mode === 'create' ? 'post' : 'put']({
        path: mode === 'create' ? '/api/panel/pages' : `/api/panel/pages/${pageData.id}`,
        body: pageData,
      });

      if (result.error) {
        if (result.error === 'PAGE_URL_ALREADY_EXISTS') {
          urlError = true;
        }
        showToast(`plugins.${pluginId}.toasts.error-saving`, {
          error: $_('errors.' + result.error),
        });
      } else {
        showToast(
          mode === 'create'
            ? `plugins.${pluginId}.toasts.page-added`
            : `plugins.${pluginId}.toasts.page-updated`,
        );

        if (mode === 'create' && result.id) {
          goto(`${base}/pages/edit/${result.id}`);
        } else {
          initialPageData = JSON.parse(JSON.stringify(pageData));
        }
      }
    } catch (e) {
      console.error(e);
      showToast(`plugins.${pluginId}.toasts.error-saving`, {
        error: $_(`errors.ERROR_GENERAL`),
      });
    } finally {
      loading = false;
    }
  }
</script>
