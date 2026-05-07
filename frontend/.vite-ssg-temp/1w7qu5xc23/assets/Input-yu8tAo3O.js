import { n as cn } from "./Button-Bj0EF1Kv.js";
import { mergeDefaults, mergeProps, unref, useSSRContext } from "vue";
import { ssrRenderAttrs } from "vue/server-renderer";
//#region src/components/ui/Input.vue
var _sfc_main = {
	__name: "Input",
	__ssrInlineRender: true,
	props: /* @__PURE__ */ mergeDefaults({
		class: String,
		type: String,
		placeholder: String,
		disabled: Boolean,
		modelValue: [String, Number]
	}, {
		type: "text",
		disabled: false
	}),
	emits: ["update:modelValue"],
	setup(__props, { emit: __emit }) {
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<input${ssrRenderAttrs(mergeProps({
				class: unref(cn)("flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50", __props.class),
				type: __props.type,
				placeholder: __props.placeholder,
				disabled: __props.disabled,
				value: __props.modelValue
			}, _attrs))}>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/Input.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as t };
