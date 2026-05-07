import { l as useConfirmStore } from "./AppLayout-D1IhsFmL.js";
//#region src/composables/useConfirm.js
/**
* Composable for showing a reusable confirmation dialog.
*
* Usage:
*   const { confirm } = useConfirm()
*   const ok = await confirm({ title: 'Delete?', description: 'This cannot be undone.' })
*   if (ok) await api.delete(`/items/${id}`)
*/
function useConfirm() {
	return { confirm: useConfirmStore().confirm };
}
//#endregion
export { useConfirm as t };
