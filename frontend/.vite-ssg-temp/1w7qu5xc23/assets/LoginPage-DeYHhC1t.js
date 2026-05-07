import { n as useAuthStore } from "../main.mjs";
import { t as _sfc_main$1 } from "./Button-Bj0EF1Kv.js";
import "./Card-ClMbbMGU.js";
import { t as _sfc_main$2 } from "./Input-yu8tAo3O.js";
import { n as _sfc_main$4, t as _sfc_main$3 } from "./Alert-DMYknBO3.js";
import { createBlock, createTextVNode, createVNode, mergeProps, openBlock, ref, toDisplayString, unref, useSSRContext, withCtx } from "vue";
import { useRouter } from "vue-router";
import { ssrInterpolate, ssrRenderAttrs, ssrRenderComponent } from "vue/server-renderer";
import { Eye, EyeOff, Loader2, Lock, LogIn, User, Zap } from "lucide-vue-next";
//#region src/pages/LoginPage.vue
var _sfc_main = {
	__name: "LoginPage",
	__ssrInlineRender: true,
	setup(__props) {
		useAuthStore();
		useRouter();
		const form = ref({
			username: "",
			password: ""
		});
		const loading = ref(false);
		const error = ref(null);
		const showPassword = ref(false);
		return (_ctx, _push, _parent, _attrs) => {
			_push(`<div${ssrRenderAttrs(mergeProps({ class: "flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-zinc-950 selection:bg-primary/20 selection:text-primary transition-colors duration-300 p-4 sm:p-6" }, _attrs))}><div class="w-full max-w-[420px] aspect-square bg-white dark:bg-zinc-900 rounded-3xl shadow-2xl border border-zinc-200 dark:border-zinc-800 flex flex-col overflow-y-auto overflow-x-hidden p-6 sm:p-8 animate-in fade-in zoom-in-95 duration-500"><div class="m-auto w-full flex flex-col space-y-6"><div class="flex items-center justify-center gap-4"><div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-primary shadow-lg shadow-primary/30 transition-colors duration-300">`);
			_push(ssrRenderComponent(unref(Zap), { class: "h-6 w-6 text-primary-foreground transition-colors duration-300" }, null, _parent));
			_push(`</div><div class="text-left"><h1 class="text-xl sm:text-2xl font-extrabold tracking-tight text-zinc-950 dark:text-zinc-100 transition-colors duration-300">Selamat Datang</h1><p class="text-xs sm:text-sm font-medium text-zinc-500 dark:text-zinc-400 transition-colors duration-300"> Silakan masuk ke akun Anda </p></div></div>`);
			if (error.value) _push(ssrRenderComponent(_sfc_main$3, {
				variant: "destructive",
				class: "animate-in zoom-in-95 duration-200 py-2.5"
			}, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) _push(`<p class="text-xs font-medium"${_scopeId}>${ssrInterpolate(error.value)}</p>`);
					else return [createVNode("p", { class: "text-xs font-medium" }, toDisplayString(error.value), 1)];
				}),
				_: 1
			}, _parent));
			else _push(`<!---->`);
			_push(`<form class="space-y-3.5 sm:space-y-4"><div class="space-y-1.5">`);
			_push(ssrRenderComponent(_sfc_main$4, {
				for: "username",
				class: "text-xs sm:text-sm font-semibold text-zinc-700 dark:text-zinc-300 transition-colors duration-300"
			}, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) _push(`Username`);
					else return [createTextVNode("Username")];
				}),
				_: 1
			}, _parent));
			_push(`<div class="group relative"><div class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none">`);
			_push(ssrRenderComponent(unref(User), { class: "h-4 w-4 text-zinc-400 group-focus-within:text-primary transition-colors" }, null, _parent));
			_push(`</div>`);
			_push(ssrRenderComponent(_sfc_main$2, {
				id: "username",
				modelValue: form.value.username,
				"onUpdate:modelValue": ($event) => form.value.username = $event,
				placeholder: "Masukkan username",
				class: "h-10 sm:h-11 pl-10 border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-950/50 focus:bg-white dark:focus:bg-zinc-900 text-zinc-900 dark:text-zinc-100 transition-all ring-offset-0 focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary shadow-sm text-sm",
				disabled: loading.value,
				required: ""
			}, null, _parent));
			_push(`</div></div><div class="space-y-1.5"><div class="flex items-center justify-between">`);
			_push(ssrRenderComponent(_sfc_main$4, {
				for: "password",
				class: "text-xs sm:text-sm font-semibold text-zinc-700 dark:text-zinc-300 transition-colors duration-300"
			}, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) _push(`Password`);
					else return [createTextVNode("Password")];
				}),
				_: 1
			}, _parent));
			_push(`</div><div class="group relative"><div class="absolute inset-y-0 left-0 flex items-center pl-3.5 pointer-events-none">`);
			_push(ssrRenderComponent(unref(Lock), { class: "h-4 w-4 text-zinc-400 group-focus-within:text-primary transition-colors" }, null, _parent));
			_push(`</div>`);
			_push(ssrRenderComponent(_sfc_main$2, {
				id: "password",
				modelValue: form.value.password,
				"onUpdate:modelValue": ($event) => form.value.password = $event,
				type: showPassword.value ? "text" : "password",
				placeholder: "Masukkan password",
				class: "h-10 sm:h-11 pl-10 pr-10 border-zinc-200 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-950/50 focus:bg-white dark:focus:bg-zinc-900 text-zinc-900 dark:text-zinc-100 transition-all ring-offset-0 focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary shadow-sm text-sm",
				disabled: loading.value,
				required: ""
			}, null, _parent));
			_push(`<button type="button" class="absolute inset-y-0 right-0 flex items-center pr-3.5 text-zinc-400 hover:text-zinc-600 focus:outline-none transition-colors" tabindex="-1">`);
			if (!showPassword.value) _push(ssrRenderComponent(unref(Eye), { class: "h-4 w-4" }, null, _parent));
			else _push(ssrRenderComponent(unref(EyeOff), { class: "h-4 w-4" }, null, _parent));
			_push(`</button></div></div><div class="pt-2 sm:pt-4">`);
			_push(ssrRenderComponent(_sfc_main$1, {
				type: "submit",
				class: "w-full h-10 sm:h-11 text-sm font-bold shadow-md hover:shadow-lg active:scale-[0.98] transition-all duration-200",
				disabled: loading.value
			}, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						if (loading.value) _push(ssrRenderComponent(unref(Loader2), { class: "mr-2 h-4 w-4 animate-spin" }, null, _parent, _scopeId));
						else _push(ssrRenderComponent(unref(LogIn), { class: "mr-2 h-4 w-4" }, null, _parent, _scopeId));
						_push(`<span${_scopeId}>Masuk</span>`);
					} else return [loading.value ? (openBlock(), createBlock(unref(Loader2), {
						key: 0,
						class: "mr-2 h-4 w-4 animate-spin"
					})) : (openBlock(), createBlock(unref(LogIn), {
						key: 1,
						class: "mr-2 h-4 w-4"
					})), createVNode("span", null, "Masuk")];
				}),
				_: 1
			}, _parent));
			_push(`</div></form><p class="text-center text-xs font-medium text-zinc-500 dark:text-zinc-400 transition-colors duration-300"> Butuh bantuan? <a href="#" class="text-primary hover:underline font-bold transition-all decoration-2 underline-offset-2">Hubungi IT Support</a></p></div></div></div>`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/LoginPage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as default };
