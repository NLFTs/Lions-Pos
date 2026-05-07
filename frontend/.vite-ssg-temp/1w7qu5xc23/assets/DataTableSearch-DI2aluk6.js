import { n as cn } from "./Button-Bj0EF1Kv.js";
import { t as _sfc_main$1 } from "./Input-yu8tAo3O.js";
import { computed, mergeProps, ref, unref, useSSRContext } from "vue";
import { ssrRenderAttrs, ssrRenderComponent } from "vue/server-renderer";
import { Search, X } from "lucide-vue-next";
//#region src/components/ui/DataTableSearch.vue
var _sfc_main = {
	__name: "DataTableSearch",
	__ssrInlineRender: true,
	props: {
		modelValue: {
			type: String,
			default: ""
		},
		placeholder: {
			type: String,
			default: "Search..."
		},
		debounce: {
			type: Number,
			default: 300
		},
		class: {
			type: [
				String,
				Object,
				Array
			],
			default: ""
		},
		inputClass: {
			type: [
				String,
				Object,
				Array
			],
			default: ""
		}
	},
	emits: ["update:modelValue", "search"],
	setup(__props, { emit: __emit }) {
		const props = __props;
		const emit = __emit;
		const localValue = ref(props.modelValue);
		let timeout = null;
		const debouncedValue = computed({
			get: () => localValue.value,
			set: (value) => {
				localValue.value = value;
				clearTimeout(timeout);
				timeout = setTimeout(() => {
					emit("update:modelValue", value);
					emit("search", value);
				}, props.debounce);
			}
		});
		const wrapperClass = computed(() => cn("relative", props.class));
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: wrapperClass.value }, _attrs))}>`);
			_push(ssrRenderComponent(unref(Search), { class: "absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" }, null, _parent));
			_push(ssrRenderComponent(_sfc_main$1, {
				modelValue: debouncedValue.value,
				"onUpdate:modelValue": ($event) => debouncedValue.value = $event,
				placeholder: __props.placeholder,
				class: unref(cn)("h-10 w-full pl-9 pr-9", props.inputClass)
			}, null, _parent));
			if (localValue.value) {
				_push(`<button class="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors">`);
				_push(ssrRenderComponent(unref(X), { class: "h-4 w-4" }, null, _parent));
				_push(`</button>`);
			} else _push(`<!---->`);
			_push(`</div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/DataTableSearch.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as t };
