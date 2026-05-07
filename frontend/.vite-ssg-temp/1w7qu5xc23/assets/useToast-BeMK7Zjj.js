import { u as useToastStore } from "./AppLayout-D1IhsFmL.js";
//#region src/composables/useToast.js
/**
* Composable for showing toast notifications.
*
* Usage:
*   const { toast } = useToast()
*   toast.success('Item created!')
*   toast.error('Something went wrong')
*   toast.info('Loading data…')
*/
function useToast() {
	const store = useToastStore();
	return { toast: {
		success: store.success,
		error: store.error,
		info: store.info
	} };
}
//#endregion
export { useToast as t };
