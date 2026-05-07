import { n as cn } from "./Button-Bj0EF1Kv.js";
import { computed, mergeProps, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderSlot } from "vue/server-renderer";
//#region src/components/ui/CardContent.vue
var _sfc_main = {
	__name: "CardContent",
	__ssrInlineRender: true,
	props: { class: String },
	setup(__props) {
		const props = __props;
		const classes = computed(() => cn("p-6 pt-0", props.class));
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: classes.value }, _attrs))}>`);
			ssrRenderSlot(_ctx.$slots, "default", {}, null, _push, _parent);
			_push(`</div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/CardContent.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as t };
