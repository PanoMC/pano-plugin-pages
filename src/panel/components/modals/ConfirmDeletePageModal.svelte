<!-- Confirm Delete Page Modal -->
<div aria-hidden="true" class="modal fade" bind:this={$modalElement} role="dialog" tabindex="-1">
  <div class="modal-dialog modal-dialog-centered" role="dialog">
    <div class="modal-content">
      <div class="modal-body text-center">
        <div class="pb-3">
          <i class="fas fa-question-circle fa-3x d-block m-auto text-gray"></i>
        </div>
        {@html $_('modals.delete.description', { values: { title: $page.title } })}
      </div>
      <div class="modal-footer flex-nowrap">
        <button
          class="btn btn-link col-6 m-0"
          type="button"
          class:disabled={loading}
          on:click={hide}>
          {$_('modals.delete.cancel')}
        </button>
        <button
          class="btn btn-danger col-6 m-0"
          type="button"
          class:disabled={loading}
          on:click={onYesClick}>
          {$_('modals.delete.confirm')}
        </button>
      </div>
    </div>
  </div>
</div>

<script context="module">
  import {get, writable} from 'svelte/store';
  import ApiUtil from '@panomc/sdk/utils/api';

  const modalElement = writable();
  const page = writable({});

  let callback = (page) => {};
  let hideCallback = (page) => {};
  let modal;

  export function show(newPage) {
    page.set(newPage);

    modal = new window.bootstrap.Modal(get(modalElement), {
      backdrop: 'static',
      keyboard: false,
    });
    modal.show();
  }

  export function setCallback(newCallback) {
    callback = newCallback;
  }

  export function hide() {
    hideCallback(get(page));
    modal.hide();
  }

  export function onHide(newCallback) {
    hideCallback = newCallback;
  }
</script>

<script>
  import { showToast } from '@panomc/sdk/toasts';
  import { _, pluginId } from '../../../main';
  let loading = false;

  async function onYesClick() {
    loading = true;

    try {
      const result = await ApiUtil.delete({
        path: `/api/panel/pages/${get(page).id}`,
      });

      if (result.error) {
        console.error('Delete failed:', result.error);
        const errorKey = `plugins.${pluginId}.errors.${result.error}`;
        const translatedError = $_(errorKey) !== errorKey 
          ? $_(errorKey) 
          : (result.error || $_(`plugins.${pluginId}.errors.ERROR_UNKNOWN`));
          
        showToast(`plugins.${pluginId}.toasts.error-deleting`, {
          error: translatedError
        });
      } else {
        showToast(`plugins.${pluginId}.toasts.page-deleted`);
        callback(get(page));
        hide();
      }
    } catch (e) {
      console.error(e);
      showToast(`plugins.${pluginId}.toasts.error-deleting`, {
        error: $_(`plugins.${pluginId}.errors.ERROR_GENERAL`),
      });
    } finally {
      loading = false;
    }
  }
</script>
