import { computed, createSSRApp, createVNode, defineComponent, isRef, onMounted, ref, resolveComponent, resolveDynamicComponent, shallowRef, toValue, unref, useSSRContext } from "vue";
import { createMemoryHistory, createRouter, createWebHistory } from "vue-router";
import { createPinia, defineStore } from "pinia";
import { ssrInterpolate, ssrRenderComponent, ssrRenderList, ssrRenderTeleport, ssrRenderVNode } from "vue/server-renderer";
import { AlertCircle, CheckCircle, Info, X } from "lucide-vue-next";
import axios from "axios";
//#region node_modules/.pnpm/hookable@6.1.1/node_modules/hookable/dist/index.mjs
function flatHooks(configHooks, hooks = {}, parentName) {
	for (const key in configHooks) {
		const subHook = configHooks[key];
		const name = parentName ? `${parentName}:${key}` : key;
		if (typeof subHook === "object" && subHook !== null) flatHooks(subHook, hooks, name);
		else if (typeof subHook === "function") hooks[name] = subHook;
	}
	return hooks;
}
var createTask = /* @__PURE__ */ (() => {
	if (console.createTask) return console.createTask;
	const defaultTask = { run: (fn) => fn() };
	return () => defaultTask;
})();
function callHooks(hooks, args, startIndex, task) {
	for (let i = startIndex; i < hooks.length; i += 1) try {
		const result = task ? task.run(() => hooks[i](...args)) : hooks[i](...args);
		if (result && typeof result.then === "function") return Promise.resolve(result).then(() => callHooks(hooks, args, i + 1, task));
	} catch (error) {
		return Promise.reject(error);
	}
}
function serialTaskCaller(hooks, args, name) {
	if (hooks.length > 0) return callHooks(hooks, args, 0, createTask(name));
}
function parallelTaskCaller(hooks, args, name) {
	if (hooks.length > 0) {
		const task = createTask(name);
		return Promise.all(hooks.map((hook) => task.run(() => hook(...args))));
	}
}
function callEachWith(callbacks, arg0) {
	for (const callback of [...callbacks]) callback(arg0);
}
var Hookable = class {
	_hooks;
	_before;
	_after;
	_deprecatedHooks;
	_deprecatedMessages;
	constructor() {
		this._hooks = {};
		this._before = void 0;
		this._after = void 0;
		this._deprecatedMessages = void 0;
		this._deprecatedHooks = {};
		this.hook = this.hook.bind(this);
		this.callHook = this.callHook.bind(this);
		this.callHookWith = this.callHookWith.bind(this);
	}
	hook(name, function_, options = {}) {
		if (!name || typeof function_ !== "function") return () => {};
		const originalName = name;
		let dep;
		while (this._deprecatedHooks[name]) {
			dep = this._deprecatedHooks[name];
			name = dep.to;
		}
		if (dep && !options.allowDeprecated) {
			let message = dep.message;
			if (!message) message = `${originalName} hook has been deprecated` + (dep.to ? `, please use ${dep.to}` : "");
			if (!this._deprecatedMessages) this._deprecatedMessages = /* @__PURE__ */ new Set();
			if (!this._deprecatedMessages.has(message)) {
				console.warn(message);
				this._deprecatedMessages.add(message);
			}
		}
		if (!function_.name) try {
			Object.defineProperty(function_, "name", {
				get: () => "_" + name.replace(/\W+/g, "_") + "_hook_cb",
				configurable: true
			});
		} catch {}
		this._hooks[name] = this._hooks[name] || [];
		this._hooks[name].push(function_);
		return () => {
			if (function_) {
				this.removeHook(name, function_);
				function_ = void 0;
			}
		};
	}
	hookOnce(name, function_) {
		let _unreg;
		let _function = (...arguments_) => {
			if (typeof _unreg === "function") _unreg();
			_unreg = void 0;
			_function = void 0;
			return function_(...arguments_);
		};
		_unreg = this.hook(name, _function);
		return _unreg;
	}
	removeHook(name, function_) {
		const hooks = this._hooks[name];
		if (hooks) {
			const index = hooks.indexOf(function_);
			if (index !== -1) hooks.splice(index, 1);
			if (hooks.length === 0) this._hooks[name] = void 0;
		}
	}
	clearHook(name) {
		this._hooks[name] = void 0;
	}
	deprecateHook(name, deprecated) {
		this._deprecatedHooks[name] = typeof deprecated === "string" ? { to: deprecated } : deprecated;
		const _hooks = this._hooks[name] || [];
		this._hooks[name] = void 0;
		for (const hook of _hooks) this.hook(name, hook);
	}
	deprecateHooks(deprecatedHooks) {
		for (const name in deprecatedHooks) this.deprecateHook(name, deprecatedHooks[name]);
	}
	addHooks(configHooks) {
		const hooks = flatHooks(configHooks);
		const removeFns = Object.keys(hooks).map((key) => this.hook(key, hooks[key]));
		return () => {
			for (const unreg of removeFns) unreg();
			removeFns.length = 0;
		};
	}
	removeHooks(configHooks) {
		const hooks = flatHooks(configHooks);
		for (const key in hooks) this.removeHook(key, hooks[key]);
	}
	removeAllHooks() {
		this._hooks = {};
	}
	callHook(name, ...args) {
		return this.callHookWith(serialTaskCaller, name, args);
	}
	callHookParallel(name, ...args) {
		return this.callHookWith(parallelTaskCaller, name, args);
	}
	callHookWith(caller, name, args) {
		const event = this._before || this._after ? {
			name,
			args,
			context: {}
		} : void 0;
		if (this._before) callEachWith(this._before, event);
		const result = caller(this._hooks[name] ? [...this._hooks[name]] : [], args, name);
		if (result instanceof Promise) return result.finally(() => {
			if (this._after && event) callEachWith(this._after, event);
		});
		if (this._after && event) callEachWith(this._after, event);
		return result;
	}
	beforeEach(function_) {
		this._before = this._before || [];
		this._before.push(function_);
		return () => {
			if (this._before !== void 0) {
				const index = this._before.indexOf(function_);
				if (index !== -1) this._before.splice(index, 1);
			}
		};
	}
	afterEach(function_) {
		this._after = this._after || [];
		this._after.push(function_);
		return () => {
			if (this._after !== void 0) {
				const index = this._after.indexOf(function_);
				if (index !== -1) this._after.splice(index, 1);
			}
		};
	}
};
function createHooks() {
	return new Hookable();
}
//#endregion
//#region node_modules/.pnpm/unhead@2.1.13/node_modules/unhead/dist/shared/unhead.yem5I2v_.mjs
var DupeableTags = /* @__PURE__ */ new Set([
	"link",
	"style",
	"script",
	"noscript"
]);
var TagsWithInnerContent = /* @__PURE__ */ new Set([
	"title",
	"titleTemplate",
	"script",
	"style",
	"noscript"
]);
var HasElementTags = /* @__PURE__ */ new Set([
	"base",
	"meta",
	"link",
	"style",
	"script",
	"noscript"
]);
var ValidHeadTags = /* @__PURE__ */ new Set([
	"title",
	"base",
	"htmlAttrs",
	"bodyAttrs",
	"meta",
	"link",
	"style",
	"script",
	"noscript"
]);
var UniqueTags = /* @__PURE__ */ new Set([
	"base",
	"title",
	"titleTemplate",
	"bodyAttrs",
	"htmlAttrs",
	"templateParams"
]);
var TagConfigKeys = /* @__PURE__ */ new Set([
	"key",
	"tagPosition",
	"tagPriority",
	"tagDuplicateStrategy",
	"innerHTML",
	"textContent",
	"processTemplateParams"
]);
var UsesMergeStrategy = /* @__PURE__ */ new Set([
	"templateParams",
	"htmlAttrs",
	"bodyAttrs"
]);
var MetaTagsArrayable = /* @__PURE__ */ new Set([
	"theme-color",
	"google-site-verification",
	"og",
	"article",
	"book",
	"profile",
	"twitter",
	"author"
]);
//#endregion
//#region node_modules/.pnpm/unhead@2.1.13/node_modules/unhead/dist/shared/unhead.B5FWS6X0.mjs
var allowedMetaProperties = [
	"name",
	"property",
	"http-equiv"
];
var StandardSingleMetaTags = /* @__PURE__ */ new Set([
	"viewport",
	"description",
	"keywords",
	"robots"
]);
function isMetaArrayDupeKey(v) {
	const parts = v.split(":");
	if (!parts.length) return false;
	return MetaTagsArrayable.has(parts[1]);
}
function dedupeKey(tag) {
	const { props, tag: name } = tag;
	if (UniqueTags.has(name)) return name;
	if (name === "link" && props.rel === "canonical") return "canonical";
	if (name === "link" && props.rel === "alternate") {
		const altKey = props.hreflang || props.type;
		if (altKey) return `alternate:${altKey}`;
	}
	if (props.charset) return "charset";
	if (tag.tag === "meta") {
		for (const n of allowedMetaProperties) if (props[n] !== void 0) {
			const propValue = props[n];
			const isStructured = propValue && typeof propValue === "string" && propValue.includes(":");
			const isStandardSingle = propValue && StandardSingleMetaTags.has(propValue);
			return `${name}:${propValue}${!(isStructured || isStandardSingle) && tag.key ? `:key:${tag.key}` : ""}`;
		}
	}
	if (tag.key) return `${name}:key:${tag.key}`;
	if (props.id) return `${name}:id:${props.id}`;
	if (name === "link" && props.rel === "alternate") return `alternate:${props.href || ""}`;
	if (TagsWithInnerContent.has(name)) {
		const v = tag.textContent || tag.innerHTML;
		if (v) return `${name}:content:${v}`;
	}
}
function hashTag(tag) {
	const dedupe = tag._h || tag._d;
	if (dedupe) return dedupe;
	const inner = tag.textContent || tag.innerHTML;
	if (inner) return inner;
	return `${tag.tag}:${Object.entries(tag.props).map(([k, v]) => `${k}:${String(v)}`).join(",")}`;
}
function walkResolver(val, resolve, key) {
	if (typeof val === "function") {
		if (!key || key !== "titleTemplate" && !(key[0] === "o" && key[1] === "n")) val = val();
	}
	const v = resolve ? resolve(key, val) : val;
	if (Array.isArray(v)) return v.map((r) => walkResolver(r, resolve));
	if (v?.constructor === Object) {
		const next = {};
		for (const k of Object.keys(v)) next[k] = walkResolver(v[k], resolve, k);
		return next;
	}
	return v;
}
function normalizeStyleClassProps(key, value) {
	const store = key === "style" ? /* @__PURE__ */ new Map() : /* @__PURE__ */ new Set();
	function processValue(rawValue) {
		if (rawValue == null || rawValue === void 0) return;
		const value2 = String(rawValue).trim();
		if (!value2) return;
		if (key === "style") {
			const [k, ...v] = value2.split(":").map((s) => s ? s.trim() : "");
			if (k && v.length) store.set(k, v.join(":"));
		} else value2.split(" ").filter(Boolean).forEach((c) => store.add(c));
	}
	if (typeof value === "string") key === "style" ? value.split(";").forEach(processValue) : processValue(value);
	else if (Array.isArray(value)) value.forEach((item) => processValue(item));
	else if (value && typeof value === "object") Object.entries(value).forEach(([k, v]) => {
		if (v && v !== "false") key === "style" ? store.set(String(k).trim(), String(v)) : processValue(k);
	});
	return store;
}
function normalizeProps(tag, input) {
	tag.props = tag.props || {};
	if (!input) return tag;
	if (tag.tag === "templateParams") {
		tag.props = input;
		return tag;
	}
	const isHtmlTag = HasElementTags.has(tag.tag) || tag.tag === "htmlAttrs" || tag.tag === "bodyAttrs";
	Object.entries(input).forEach(([prop, value]) => {
		if (prop === "__proto__" || prop === "constructor" || prop === "prototype") return;
		if (value === null) {
			tag.props[prop] = null;
			return;
		}
		if (prop === "class" || prop === "style") {
			tag.props[prop] = normalizeStyleClassProps(prop, value);
			return;
		}
		if (TagConfigKeys.has(prop)) {
			if ((prop === "textContent" || prop === "innerHTML") && typeof value === "object") {
				let type = input.type;
				if (!input.type) type = "application/json";
				if (!type?.endsWith("json") && type !== "speculationrules") return;
				input.type = type;
				tag.props.type = type;
				tag[prop] = JSON.stringify(value);
			} else tag[prop] = value;
			return;
		}
		const isDataKey = prop.startsWith("data-");
		const key = isHtmlTag && !isDataKey ? prop.toLowerCase() : prop;
		const strValue = String(value);
		const isMetaContentKey = tag.tag === "meta" && key === "content";
		if (strValue === "true" || strValue === "") tag.props[key] = isDataKey || isMetaContentKey ? strValue : true;
		else if (!value && isDataKey && strValue === "false") tag.props[key] = "false";
		else if (value !== void 0) tag.props[key] = value;
	});
	return tag;
}
function normalizeTag(tagName, _input) {
	const tag = normalizeProps({
		tag: tagName,
		props: {}
	}, typeof _input === "object" && typeof _input !== "function" ? _input : { [tagName === "script" || tagName === "noscript" || tagName === "style" ? "innerHTML" : "textContent"]: _input });
	if (tag.key && DupeableTags.has(tag.tag)) tag.props["data-hid"] = tag._h = tag.key;
	if (tag.tag === "script" && typeof tag.innerHTML === "object") {
		tag.innerHTML = JSON.stringify(tag.innerHTML);
		tag.props.type = tag.props.type || "application/json";
	}
	return Array.isArray(tag.props.content) ? tag.props.content.map((v) => ({
		...tag,
		props: {
			...tag.props,
			content: v
		}
	})) : tag;
}
function normalizeEntryToTags(input, propResolvers) {
	if (!input) return [];
	if (typeof input === "function") input = input();
	const resolvers = (key, val) => {
		for (let i = 0; i < propResolvers.length; i++) val = propResolvers[i](key, val);
		return val;
	};
	input = resolvers(void 0, input);
	const tags = [];
	input = walkResolver(input, resolvers);
	Object.entries(input || {}).forEach(([key, value]) => {
		if (value === void 0) return;
		for (const v of Array.isArray(value) ? value : [value]) tags.push(normalizeTag(key, v));
	});
	return tags.flat();
}
//#endregion
//#region node_modules/.pnpm/unhead@2.1.13/node_modules/unhead/dist/shared/unhead.CbpEuj3y.mjs
var sortTags = (a, b) => a._w === b._w ? a._p - b._p : a._w - b._w;
var TAG_WEIGHTS = {
	base: -10,
	title: 10
};
var TAG_ALIASES = {
	critical: -8,
	high: -1,
	low: 2
};
var WEIGHT_MAP = {
	meta: {
		"content-security-policy": -30,
		"charset": -20,
		"viewport": -15
	},
	link: {
		"preconnect": 20,
		"stylesheet": 60,
		"preload": 70,
		"modulepreload": 70,
		"prefetch": 90,
		"dns-prefetch": 90,
		"prerender": 90
	},
	script: {
		async: 30,
		defer: 80,
		sync: 50
	},
	style: {
		imported: 40,
		sync: 60
	}
};
var ImportStyleRe = /@import/;
var isTruthy = (val) => val === "" || val === true;
function tagWeight(head, tag) {
	if (typeof tag.tagPriority === "number") return tag.tagPriority;
	let weight = 100;
	const offset = TAG_ALIASES[tag.tagPriority] || 0;
	const weightMap = head.resolvedOptions.disableCapoSorting ? {
		link: {},
		script: {},
		style: {}
	} : WEIGHT_MAP;
	if (tag.tag in TAG_WEIGHTS) weight = TAG_WEIGHTS[tag.tag];
	else if (tag.tag === "meta") {
		const metaType = tag.props["http-equiv"] === "content-security-policy" ? "content-security-policy" : tag.props.charset ? "charset" : tag.props.name === "viewport" ? "viewport" : null;
		if (metaType) weight = WEIGHT_MAP.meta[metaType];
	} else if (tag.tag === "link" && tag.props.rel) weight = weightMap.link[tag.props.rel];
	else if (tag.tag === "script") {
		const type = String(tag.props.type);
		if (isTruthy(tag.props.async)) weight = weightMap.script.async;
		else if (tag.props.src && !isTruthy(tag.props.defer) && !isTruthy(tag.props.async) && type !== "module" && !type.endsWith("json") || tag.innerHTML && !type.endsWith("json")) weight = weightMap.script.sync;
		else if (isTruthy(tag.props.defer) && tag.props.src && !isTruthy(tag.props.async) || type === "module") weight = weightMap.script.defer;
	} else if (tag.tag === "style") weight = tag.innerHTML && ImportStyleRe.test(tag.innerHTML) ? weightMap.style.imported : weightMap.style.sync;
	return (weight || 100) + offset;
}
//#endregion
//#region node_modules/.pnpm/unhead@2.1.13/node_modules/unhead/dist/shared/unhead.Ct24BOby.mjs
function registerPlugin(head, p) {
	const plugin = typeof p === "function" ? p(head) : p;
	const key = plugin.key || String(head.plugins.size + 1);
	if (!head.plugins.get(key)) {
		head.plugins.set(key, plugin);
		head.hooks.addHooks(plugin.hooks || {});
	}
}
/* @__NO_SIDE_EFFECTS__ */
function createUnhead(resolvedOptions = {}) {
	const hooks = createHooks();
	hooks.addHooks(resolvedOptions.hooks || {});
	const ssr = !resolvedOptions.document;
	const entries = /* @__PURE__ */ new Map();
	const plugins = /* @__PURE__ */ new Map();
	const normalizeQueue = /* @__PURE__ */ new Set();
	const head = {
		_entryCount: 1,
		plugins,
		dirty: false,
		resolvedOptions,
		hooks,
		ssr,
		entries,
		headEntries() {
			return [...entries.values()];
		},
		use: (p) => registerPlugin(head, p),
		push(input, _options) {
			const options = { ..._options || {} };
			delete options.head;
			const _i = options._index ?? head._entryCount++;
			const inst = {
				_i,
				input,
				options
			};
			const _ = {
				_poll(rm = false) {
					head.dirty = true;
					!rm && normalizeQueue.add(_i);
					hooks.callHook("entries:updated", head);
				},
				dispose() {
					if (entries.delete(_i)) head.invalidate();
				},
				patch(input2) {
					if (!options.mode || options.mode === "server" && ssr || options.mode === "client" && !ssr) {
						inst.input = input2;
						entries.set(_i, inst);
						_._poll();
					}
				}
			};
			_.patch(input);
			return _;
		},
		async resolveTags() {
			const ctx = {
				tagMap: /* @__PURE__ */ new Map(),
				tags: [],
				entries: [...head.entries.values()]
			};
			await hooks.callHook("entries:resolve", ctx);
			while (normalizeQueue.size) {
				const i = normalizeQueue.values().next().value;
				normalizeQueue.delete(i);
				const e = entries.get(i);
				if (e) {
					const normalizeCtx = {
						tags: normalizeEntryToTags(e.input, resolvedOptions.propResolvers || []).map((t) => Object.assign(t, e.options)),
						entry: e
					};
					await hooks.callHook("entries:normalize", normalizeCtx);
					e._tags = normalizeCtx.tags.map((t, i2) => {
						t._w = tagWeight(head, t);
						t._p = (e._i << 10) + i2;
						t._d = dedupeKey(t);
						if (!t._d) t._h = hashTag(t);
						return t;
					});
				}
			}
			let hasFlatMeta = false;
			ctx.entries.flatMap((e) => (e._tags || []).map((t) => ({
				...t,
				props: { ...t.props }
			}))).sort(sortTags).reduce((acc, next) => {
				const k = next._d || next._h;
				if (!acc.has(k)) return acc.set(k, next);
				const prev = acc.get(k);
				if ((next?.tagDuplicateStrategy || (UsesMergeStrategy.has(next.tag) ? "merge" : null) || (next.key && next.key === prev.key ? "merge" : null)) === "merge") {
					const newProps = { ...prev.props };
					Object.entries(next.props).forEach(([p, v]) => newProps[p] = p === "style" ? new Map([...prev.props.style || /* @__PURE__ */ new Map(), ...v]) : p === "class" ? /* @__PURE__ */ new Set([...prev.props.class || /* @__PURE__ */ new Set(), ...v]) : v);
					acc.set(k, {
						...next,
						props: newProps
					});
				} else if (next._p >> 10 === prev._p >> 10 && next.tag === "meta" && isMetaArrayDupeKey(k)) {
					acc.set(k, Object.assign([...Array.isArray(prev) ? prev : [prev], next], next));
					hasFlatMeta = true;
				} else if (next._w === prev._w ? next._p > prev._p : next?._w < prev?._w) acc.set(k, next);
				return acc;
			}, ctx.tagMap);
			const title = ctx.tagMap.get("title");
			const titleTemplate = ctx.tagMap.get("titleTemplate");
			head._title = title?.textContent;
			if (titleTemplate) {
				const titleTemplateFn = titleTemplate?.textContent;
				head._titleTemplate = titleTemplateFn;
				if (titleTemplateFn) {
					let newTitle = typeof titleTemplateFn === "function" ? titleTemplateFn(title?.textContent) : titleTemplateFn;
					if (typeof newTitle === "string" && !head.plugins.has("template-params")) newTitle = newTitle.replace("%s", title?.textContent || "");
					if (title) newTitle === null ? ctx.tagMap.delete("title") : ctx.tagMap.set("title", {
						...title,
						textContent: newTitle
					});
					else {
						titleTemplate.tag = "title";
						titleTemplate.textContent = newTitle;
					}
				}
			}
			ctx.tags = Array.from(ctx.tagMap.values());
			if (hasFlatMeta) ctx.tags = ctx.tags.flat().sort(sortTags);
			await hooks.callHook("tags:beforeResolve", ctx);
			await hooks.callHook("tags:resolve", ctx);
			await hooks.callHook("tags:afterResolve", ctx);
			const finalTags = [];
			for (const t of ctx.tags) {
				const { innerHTML, tag, props } = t;
				if (!ValidHeadTags.has(tag)) continue;
				if (Object.keys(props).length === 0 && !t.innerHTML && !t.textContent) continue;
				if (tag === "meta" && !props.content && !props["http-equiv"] && !props.charset) continue;
				if (tag === "script" && innerHTML) {
					if (String(props.type).endsWith("json")) t.innerHTML = (typeof innerHTML === "string" ? innerHTML : JSON.stringify(innerHTML)).replace(/</g, "\\u003C");
					else if (typeof innerHTML === "string") t.innerHTML = innerHTML.replace(new RegExp(`</${tag}`, "g"), `<\\/${tag}`);
					t._d = dedupeKey(t);
				}
				finalTags.push(t);
			}
			return finalTags;
		},
		invalidate() {
			for (const entry of entries.values()) normalizeQueue.add(entry._i);
			head.dirty = true;
			hooks.callHook("entries:updated", head);
		}
	};
	(resolvedOptions?.plugins || []).forEach((p) => registerPlugin(head, p));
	head.hooks.callHook("init", head);
	resolvedOptions.init?.forEach((e) => e && head.push(e));
	return head;
}
//#endregion
//#region node_modules/.pnpm/@unhead+vue@2.1.13_vue@3.5.33/node_modules/@unhead/vue/dist/shared/vue.N9zWjxoK.mjs
var VueResolver = (_, value) => {
	return isRef(value) ? toValue(value) : value;
};
//#endregion
//#region node_modules/.pnpm/@unhead+vue@2.1.13_vue@3.5.33/node_modules/@unhead/vue/dist/shared/vue.Cr7xSEtD.mjs
var headSymbol = "usehead";
/* @__NO_SIDE_EFFECTS__ */
function vueInstall(head) {
	return { install(app) {
		app.config.globalProperties.$unhead = head;
		app.config.globalProperties.$head = head;
		app.provide(headSymbol, head);
	} }.install;
}
//#endregion
//#region node_modules/.pnpm/unhead@2.1.13/node_modules/unhead/dist/server.mjs
/* @__NO_SIDE_EFFECTS__ */
function createHead$1(options = {}) {
	const unhead = /* @__PURE__ */ createUnhead({
		...options,
		document: false,
		propResolvers: [...options.propResolvers || [], (k, v) => {
			if (k && k.startsWith("on") && typeof v === "function") return `this.dataset.${k}fired = true`;
			return v;
		}],
		init: [options.disableDefaults ? void 0 : {
			htmlAttrs: { lang: "en" },
			meta: [{ charset: "utf-8" }, {
				name: "viewport",
				content: "width=device-width, initial-scale=1"
			}]
		}, ...options.init || []]
	});
	unhead._ssrPayload = {};
	unhead.use({
		key: "server",
		hooks: { "tags:resolve": function(ctx) {
			const title = ctx.tagMap.get("title");
			const titleTemplate = ctx.tagMap.get("titleTemplate");
			let payload = {
				title: title?.mode === "server" ? unhead._title : void 0,
				titleTemplate: titleTemplate?.mode === "server" ? unhead._titleTemplate : void 0
			};
			if (Object.keys(unhead._ssrPayload || {}).length > 0) payload = {
				...unhead._ssrPayload,
				...payload
			};
			if (Object.values(payload).some(Boolean)) ctx.tags.push({
				tag: "script",
				innerHTML: JSON.stringify(payload),
				props: {
					id: "unhead:payload",
					type: "application/json"
				}
			});
		} }
	});
	return unhead;
}
//#endregion
//#region node_modules/.pnpm/@unhead+vue@2.1.13_vue@3.5.33/node_modules/@unhead/vue/dist/server.mjs
/* @__NO_SIDE_EFFECTS__ */
function createHead(options = {}) {
	const head = /* @__PURE__ */ createHead$1({
		...options,
		propResolvers: [VueResolver]
	});
	head.install = /* @__PURE__ */ vueInstall(head);
	return head;
}
//#endregion
//#region node_modules/.pnpm/vite-ssg@28.3.0_unhead@2.1.13_vite@8.0.10_jiti@1.21.7_terser@5.46.2_yaml@2.8.3__vue-rou_7094933eb2b71b4c8de79e2d6164f475/node_modules/vite-ssg/dist/shared/vite-ssg.ETIvV-80.mjs
var ClientOnly = defineComponent({ setup(props, { slots }) {
	const mounted = ref(false);
	onMounted(() => mounted.value = true);
	return () => {
		if (!mounted.value) return slots.placeholder && slots.placeholder({});
		return slots.default && slots.default({});
	};
} });
//#endregion
//#region node_modules/.pnpm/vite-ssg@28.3.0_unhead@2.1.13_vite@8.0.10_jiti@1.21.7_terser@5.46.2_yaml@2.8.3__vue-rou_7094933eb2b71b4c8de79e2d6164f475/node_modules/vite-ssg/dist/index.mjs
function ViteSSG(App, routerOptions, fn, options) {
	const { transformState, registerComponents = true, useHead = true, rootContainer = "#app" } = options ?? {};
	async function createApp$1(routePath) {
		const app = createSSRApp(App);
		let head;
		if (useHead) app.use(head = /* @__PURE__ */ createHead());
		const router = createRouter({
			history: createMemoryHistory(routerOptions.base),
			...routerOptions
		});
		const { routes } = routerOptions;
		if (registerComponents) app.component("ClientOnly", ClientOnly);
		const appRenderCallbacks = [];
		const onSSRAppRendered = (cb) => appRenderCallbacks.push(cb);
		const triggerOnSSRAppRendered = () => {
			return Promise.all(appRenderCallbacks.map((cb) => cb()));
		};
		const context = {
			app,
			head,
			isClient: false,
			router,
			routes,
			onSSRAppRendered,
			triggerOnSSRAppRendered,
			initialState: {},
			transformState,
			routePath
		};
		await fn?.(context);
		app.use(router);
		let entryRoutePath;
		let isFirstRoute = true;
		router.beforeEach((to, from, next) => {
			if (isFirstRoute || entryRoutePath && entryRoutePath === to.path) {
				isFirstRoute = false;
				entryRoutePath = to.path;
				to.meta.state = context.initialState;
			}
			next();
		});
		{
			const route = context.routePath ?? "/";
			router.push(route);
			await router.isReady();
			context.initialState = router.currentRoute.value.meta.state || {};
		}
		const initialState = context.initialState;
		return {
			...context,
			initialState
		};
	}
	return createApp$1;
}
//#endregion
//#region src/composables/useNotification.js
var notifications = ref([]);
function useNotification() {
	function notify({ title, message, type = "default", duration = 3e3 }) {
		const id = Date.now().toString() + Math.random().toString(36).substr(2, 5);
		notifications.value.push({
			id,
			title,
			message,
			type
		});
		if (duration > 0) setTimeout(() => {
			remove(id);
		}, duration);
	}
	function remove(id) {
		const index = notifications.value.findIndex((n) => n.id === id);
		if (index !== -1) notifications.value.splice(index, 1);
	}
	return {
		notifications,
		notify,
		remove
	};
}
//#endregion
//#region src/components/ui/NotificationProvider.vue
var _sfc_main$1 = {
	__name: "NotificationProvider",
	__ssrInlineRender: true,
	setup(__props) {
		const { notifications, remove } = useNotification();
		function getIcon(type) {
			if (type === "success") return CheckCircle;
			if (type === "error") return AlertCircle;
			return Info;
		}
		function getIconClass(type) {
			if (type === "success") return "text-emerald-500";
			if (type === "error") return "text-red-500";
			return "text-zinc-500";
		}
		return (_ctx, _push, _parent, _attrs) => {
			ssrRenderTeleport(_push, (_push) => {
				_push(`<div class="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 max-w-[380px] w-full px-4 sm:px-0 pointer-events-none"><!--[-->`);
				ssrRenderList(unref(notifications), (item) => {
					_push(`<div class="flex items-start gap-3 w-full bg-white dark:bg-zinc-950 border border-zinc-200 dark:border-zinc-800 shadow-[0_8px_30px_rgba(0,0,0,0.12)] rounded-lg p-3.5 pointer-events-auto">`);
					ssrRenderVNode(_push, createVNode(resolveDynamicComponent(getIcon(item.type)), { class: ["w-4 h-4 shrink-0 mt-0.5", getIconClass(item.type)] }, null), _parent);
					_push(`<div class="flex-1 min-w-0">`);
					if (item.title) _push(`<h4 class="text-[13px] font-semibold text-zinc-900 dark:text-zinc-100">${ssrInterpolate(item.title)}</h4>`);
					else _push(`<!---->`);
					if (item.message) _push(`<p class="text-[12px] text-zinc-500 dark:text-zinc-400 mt-0.5 leading-snug">${ssrInterpolate(item.message)}</p>`);
					else _push(`<!---->`);
					_push(`</div><button class="text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100 transition-colors shrink-0">`);
					_push(ssrRenderComponent(unref(X), { class: "w-3.5 h-3.5" }, null, _parent));
					_push(`</button></div>`);
				});
				_push(`<!--]--></div>`);
			}, "body", false, _parent);
		};
	}
};
var _sfc_setup$1 = _sfc_main$1.setup;
_sfc_main$1.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/components/ui/NotificationProvider.vue");
	return _sfc_setup$1 ? _sfc_setup$1(props, ctx) : void 0;
};
//#endregion
//#region src/App.vue
var _sfc_main = {
	__name: "App",
	__ssrInlineRender: true,
	setup(__props) {
		return (_ctx, _push, _parent, _attrs) => {
			const _component_RouterView = resolveComponent("RouterView");
			_push(`<!--[-->`);
			_push(ssrRenderComponent(_component_RouterView, null, null, _parent));
			_push(ssrRenderComponent(_sfc_main$1, null, null, _parent));
			_push(`<!--]-->`);
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/App.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
//#region src/lib/mock.js
/**
* Mocking layer for GAPETK Frontend
* This allows the frontend to run without a backend.
*/
var permissionModules = [
	"user",
	"produk",
	"category",
	"stock-mutation",
	"partner",
	"location",
	"voucher",
	"role",
	"permission",
	"module"
];
var permissionActions = [
	"index",
	"show",
	"store",
	"update",
	"delete"
];
var mockPermissionsMap = permissionModules.reduce((acc, module) => {
	acc[module] = permissionActions.map((action) => ({
		id: `perm-${module}-${action}`,
		name: `${action.charAt(0).toUpperCase()}${action.slice(1)} ${module}`,
		slug: `${module}.${action}`,
		module,
		action
	}));
	return acc;
}, { log: [{
	id: "perm-log-index",
	name: "Index log",
	slug: "log.index",
	module: "log",
	action: "index"
}] });
var allMockPermissions = Object.values(mockPermissionsMap).flat();
var editorPermissions = allMockPermissions.filter((permission) => ["produk", "category"].includes(permission.module) || permission.slug === "log.index");
function parseBody(data) {
	if (!data) return {};
	if (typeof data === "string") try {
		return JSON.parse(data);
	} catch {
		return {};
	}
	return data;
}
function mockRoleMutation(config, url) {
	const method = (config.method || "get").toLowerCase();
	const body = parseBody(config.data);
	if (method === "post" && url === "/api/v1/roles") {
		const permissions = allMockPermissions.filter((permission) => body.permissionIds?.includes(permission.id));
		return {
			status: 200,
			data: { data: {
				id: `r-${Date.now()}`,
				name: body.name,
				slug: body.slug,
				permissions
			} }
		};
	}
	const permissionMatch = url.match(/^\/api\/v1\/roles\/([^/]+)\/permissions$/);
	if (method === "put" && permissionMatch) {
		const permissions = allMockPermissions.filter((permission) => body.permissionIds?.includes(permission.id));
		return {
			status: 200,
			data: { data: {
				id: permissionMatch[1],
				permissions
			} }
		};
	}
	const roleMatch = url.match(/^\/api\/v1\/roles\/([^/]+)$/);
	if (roleMatch && ["put", "delete"].includes(method)) {
		if (method === "delete") return {
			status: 204,
			data: null
		};
		return {
			status: 200,
			data: { data: {
				id: roleMatch[1],
				name: body.name,
				slug: body.slug
			} }
		};
	}
	return null;
}
var mockData = {
	"/api/v1/auth/me": {
		status: 200,
		data: { data: {
			id: "mock-user-1",
			username: "admin",
			fullname: "Super Admin Mock",
			roles: ["ADMIN"],
			permissions: [
				"user.index",
				"user.store",
				"user.update",
				"user.delete",
				"user.destroy",
				"produk.index",
				"produk.store",
				"produk.update",
				"produk.delete",
				"produk.destroy",
				"category.index",
				"category.store",
				"category.update",
				"category.delete",
				"category.destroy",
				"stock-mutation.index",
				"stock-mutation.store",
				"stock-mutation.update",
				"stock-mutation.delete",
				"stock-mutation.destroy",
				"partner.index",
				"partner.store",
				"partner.update",
				"partner.delete",
				"partner.destroy",
				"location.index",
				"location.store",
				"location.update",
				"location.delete",
				"location.destroy",
				"voucher.index",
				"voucher.store",
				"voucher.update",
				"voucher.delete",
				"voucher.destroy",
				"role.index",
				"role.store",
				"role.update",
				"role.delete",
				"role.destroy",
				"permission.index",
				"permission.store",
				"permission.update",
				"permission.delete",
				"permission.destroy",
				"module.index",
				"module.store",
				"module.update",
				"module.delete",
				"module.destroy",
				"log.index"
			]
		} }
	},
	"/api/v1/auth/login": {
		status: 200,
		data: { data: {
			accessToken: "mock-access-token",
			refreshToken: "mock-refresh-token"
		} }
	},
	"/api/v1/users": {
		status: 200,
		data: { data: [
			{
				id: "1",
				username: "john_doe",
				fullname: "John Doe",
				phone: "6281234567890",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=john_doe",
				roles: [{
					id: "r1",
					name: "ADMIN",
					slug: "admin"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-01-15")).toISOString()
			},
			{
				id: "2",
				username: "jane_smith",
				fullname: "Jane Smith",
				phone: "6289876543210",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=jane_smith",
				roles: [{
					id: "r2",
					name: "EDITOR",
					slug: "editor"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-02-20")).toISOString()
			},
			{
				id: "3",
				username: "bob_builder",
				fullname: "Bob Builder",
				phone: "6282117227065",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=bob_builder",
				roles: [{
					id: "r2",
					name: "EDITOR",
					slug: "editor"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-03-10")).toISOString()
			},
			{
				id: "4",
				username: "alice_wonder",
				fullname: "Alice Wonderland",
				phone: "6285551234567",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=alice_wonder",
				roles: [{
					id: "r1",
					name: "ADMIN",
					slug: "admin"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-04-05")).toISOString()
			},
			{
				id: "5",
				username: "charlie_brown",
				fullname: "Charlie Brown",
				phone: "6287654321098",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=charlie_brown",
				roles: [{
					id: "r2",
					name: "EDITOR",
					slug: "editor"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-05-18")).toISOString()
			},
			{
				id: "6",
				username: "diana_prince",
				fullname: "Diana Prince",
				phone: "6281122334455",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=diana_prince",
				roles: [{
					id: "r1",
					name: "ADMIN",
					slug: "admin"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-06-22")).toISOString()
			},
			{
				id: "7",
				username: "evan_rogers",
				fullname: "Evan Rogers",
				phone: "6289911223344",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=evan_rogers",
				roles: [{
					id: "r2",
					name: "EDITOR",
					slug: "editor"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-07-30")).toISOString()
			},
			{
				id: "8",
				username: "fiona_green",
				fullname: "Fiona Green",
				phone: "6283344556677",
				avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=fiona_green",
				roles: [{
					id: "r2",
					name: "EDITOR",
					slug: "editor"
				}],
				createdAt: (/* @__PURE__ */ new Date("2024-08-14")).toISOString()
			}
		] }
	},
	"/api/v1/posts?page=0&size=5": {
		status: 200,
		data: { data: {
			totalElements: 12,
			content: [
				{
					id: "p1",
					title: "Cara Menggunakan Spravel",
					status: "published",
					createdAt: (/* @__PURE__ */ new Date()).toISOString()
				},
				{
					id: "p2",
					title: "Tips Desain UI Modern",
					status: "draft",
					createdAt: (/* @__PURE__ */ new Date(Date.now() - 36e5)).toISOString()
				},
				{
					id: "p3",
					title: "Panduan Vue 3 Composition API",
					status: "published",
					createdAt: (/* @__PURE__ */ new Date(Date.now() - 72e5)).toISOString()
				}
			]
		} }
	},
	"/api/v1/categories": {
		status: 200,
		data: { data: [
			{
				id: "c1",
				name: "Tutorial",
				slug: "tutorial"
			},
			{
				id: "c2",
				name: "Design",
				slug: "design"
			},
			{
				id: "c3",
				name: "News",
				slug: "news"
			}
		] }
	},
	"/api/v1/roles": {
		status: 200,
		data: { data: [{
			id: "r1",
			name: "ADMIN",
			slug: "admin",
			description: "Full access",
			permissions: allMockPermissions
		}, {
			id: "r2",
			name: "EDITOR",
			slug: "editor",
			description: "Can edit posts",
			permissions: editorPermissions
		}] }
	},
	"/api/v1/roles/permissions": {
		status: 200,
		data: { data: mockPermissionsMap }
	},
	"/api/v1/permissions": {
		status: 200,
		data: { data: allMockPermissions }
	},
	"/api/v1/modules": {
		status: 200,
		data: { data: [{
			id: "m1",
			name: "User Management"
		}, {
			id: "m2",
			name: "Content Management"
		}] }
	},
	"/api/v1/logs?page=0&size=20": {
		status: 200,
		data: { data: {
			totalElements: 4,
			totalPages: 1,
			number: 0,
			size: 20,
			content: [
				{
					id: "log1",
					method: "POST",
					url: "/api/v1/auth/login",
					responseStatus: 200,
					userFullname: "Super Admin Mock",
					userId: "mock-user-1",
					durationMs: 45,
					requestAt: (/* @__PURE__ */ new Date()).toISOString(),
					responseAt: (/* @__PURE__ */ new Date()).toISOString(),
					userAgent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/115.0.0.0 Safari/537.36"
				},
				{
					id: "log2",
					method: "GET",
					url: "/api/v1/users",
					responseStatus: 200,
					userFullname: "Super Admin Mock",
					userId: "mock-user-1",
					durationMs: 120,
					requestAt: (/* @__PURE__ */ new Date(Date.now() - 36e5)).toISOString(),
					responseAt: (/* @__PURE__ */ new Date(Date.now() - 36e5 + 120)).toISOString(),
					userAgent: "Mozilla/5.0"
				},
				{
					id: "log3",
					method: "PUT",
					url: "/api/v1/settings/theme",
					responseStatus: 204,
					userFullname: "Jane Smith",
					userId: "2",
					durationMs: 80,
					requestAt: (/* @__PURE__ */ new Date(Date.now() - 72e5)).toISOString(),
					responseAt: (/* @__PURE__ */ new Date(Date.now() - 72e5 + 80)).toISOString(),
					userAgent: "PostmanRuntime/7.28.4"
				},
				{
					id: "log4",
					method: "DELETE",
					url: "/api/v1/posts/p1",
					responseStatus: 403,
					userFullname: "Bob Builder",
					userId: "3",
					durationMs: 35,
					requestAt: (/* @__PURE__ */ new Date(Date.now() - 864e5)).toISOString(),
					responseAt: (/* @__PURE__ */ new Date(Date.now() - 864e5 + 35)).toISOString(),
					userAgent: "curl/7.68.0"
				}
			]
		} }
	}
};
function setupMocks(api) {
	api.interceptors.request.use(async (config) => {
		const url = config.url.replace(config.baseURL, "");
		const roleMutationResponse = mockRoleMutation(config, url);
		if (roleMutationResponse) {
			console.log(`[Mock API] Intercepting ${config.method.toUpperCase()} ${url}`);
			return Promise.reject({
				__isMock: true,
				response: roleMutationResponse
			});
		}
		if (mockData[url]) {
			console.log(`[Mock API] Intercepting ${config.method.toUpperCase()} ${url}`);
			return Promise.reject({
				__isMock: true,
				response: mockData[url]
			});
		}
		const baseUrl = url.split("?")[0];
		if (mockData[baseUrl]) {
			const mockResponse = mockData[baseUrl];
			const rawData = mockResponse.data?.data;
			if (Array.isArray(rawData)) {
				const params = new URLSearchParams(url.split("?")[1] || "");
				const page = parseInt(params.get("page") || "0");
				const size = parseInt(params.get("size") || "10");
				const sliced = rawData.slice(page * size, page * size + size);
				const paginatedResponse = {
					...mockResponse,
					data: { data: {
						content: sliced,
						totalElements: rawData.length,
						totalPages: Math.ceil(rawData.length / size),
						number: page,
						size
					} }
				};
				console.log(`[Mock API] Intercepting (paginated) ${config.method.toUpperCase()} ${url}`);
				return Promise.reject({
					__isMock: true,
					response: paginatedResponse
				});
			}
			console.log(`[Mock API] Intercepting (prefix) ${config.method.toUpperCase()} ${url}`);
			return Promise.reject({
				__isMock: true,
				response: mockResponse
			});
		}
		return config;
	});
	api.interceptors.response.use((response) => response, (error) => {
		if (error.__isMock) return Promise.resolve(error.response);
		return Promise.reject(error);
	});
}
var isMockMode = false;
var isEmptyMode = false;
var FULL_PERMISSIONS = [
	"user.index",
	"user.store",
	"user.update",
	"user.delete",
	"user.destroy",
	"produk.index",
	"produk.store",
	"produk.update",
	"produk.delete",
	"produk.destroy",
	"category.index",
	"category.store",
	"category.update",
	"category.delete",
	"category.destroy",
	"role.index",
	"role.store",
	"role.update",
	"role.delete",
	"role.destroy",
	"permission.index",
	"permission.store",
	"permission.update",
	"permission.delete",
	"permission.destroy",
	"module.index",
	"module.store",
	"module.update",
	"module.delete",
	"module.destroy",
	"log.index"
];
//#endregion
//#region src/lib/empty.js
/**
* Empty data layer.
* Use this for a clean local UI without sample mock data and without backend calls.
*/
var emptyUser = {
	id: "empty-user",
	username: "admin",
	fullname: "Empty Admin",
	roles: ["ADMIN"],
	permissions: FULL_PERMISSIONS
};
function paginatedData(config) {
	const url = config.url || "";
	const params = new URLSearchParams(url.split("?")[1] || "");
	return {
		content: [],
		totalElements: 0,
		totalPages: 0,
		number: parseInt(params.get("page") || "0"),
		size: parseInt(params.get("size") || "10")
	};
}
function emptyResponse(config) {
	const url = config.url || "";
	const method = (config.method || "get").toLowerCase();
	const path = url.split("?")[0];
	if (path === "/api/v1/auth/login") return {
		status: 200,
		data: { data: {
			accessToken: "empty-access-token",
			refreshToken: "empty-refresh-token"
		} }
	};
	if (path === "/api/v1/auth/me") return {
		status: 200,
		data: { data: emptyUser }
	};
	if (path === "/api/v1/auth/logout") return {
		status: 204,
		data: null
	};
	if (method === "get") return {
		status: 200,
		data: { data: url.includes("page=") || [
			"/api/v1/posts",
			"/api/v1/logs",
			"/api/v1/users"
		].includes(path) ? paginatedData(config) : [] }
	};
	if (method === "delete") return {
		status: 204,
		data: null
	};
	return {
		status: 200,
		data: { data: {} }
	};
}
function setupEmptyData(api) {
	api.interceptors.request.use((config) => {
		return Promise.reject({
			__isEmptyData: true,
			response: emptyResponse(config)
		});
	});
	api.interceptors.response.use((response) => response, (error) => {
		if (error.__isEmptyData) return Promise.resolve(error.response);
		return Promise.reject(error);
	});
}
//#endregion
//#region src/lib/api.js
var api = axios.create({
	baseURL: "",
	headers: { "Content-Type": "application/json" }
});
if (isEmptyMode) setupEmptyData(api);
else if (isMockMode) setupMocks(api);
function parseJwtPerms(token) {
	if (isMockMode && token === "mock-access-token" || isEmptyMode && token === "empty-access-token") return FULL_PERMISSIONS;
	try {
		const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
		return Array.isArray(payload.perms) ? payload.perms : [];
	} catch {
		return [];
	}
}
api.interceptors.request.use((config) => {
	const token = localStorage.getItem("access_token");
	if (token) config.headers.Authorization = `Bearer ${token}`;
	return config;
});
var isRefreshing = false;
var failedQueue = [];
var processQueue = (error, token = null) => {
	failedQueue.forEach((prom) => {
		if (error) prom.reject(error);
		else prom.resolve(token);
	});
	failedQueue = [];
};
api.interceptors.response.use((response) => response, async (error) => {
	const originalRequest = error.config;
	if (error.response?.status === 401 && !originalRequest._retry) {
		if (isRefreshing) return new Promise((resolve, reject) => {
			failedQueue.push({
				resolve,
				reject
			});
		}).then((token) => {
			originalRequest.headers.Authorization = `Bearer ${token}`;
			return api(originalRequest);
		}).catch((err) => Promise.reject(err));
		originalRequest._retry = true;
		isRefreshing = true;
		const refreshToken = localStorage.getItem("refresh_token");
		if (!refreshToken) {
			useAuthStore().logout();
			return Promise.reject(error);
		}
		try {
			const newAccessToken = (await axios.post(`/api/v1/auth/refresh?refreshToken=${refreshToken}`)).data.data.accessToken;
			localStorage.setItem("access_token", newAccessToken);
			api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
			const auth = useAuthStore();
			auth.accessToken = newAccessToken;
			const newPerms = parseJwtPerms(newAccessToken);
			auth.permissions = newPerms;
			localStorage.setItem("auth_permissions", JSON.stringify(newPerms));
			processQueue(null, newAccessToken);
			originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
			return api(originalRequest);
		} catch (err) {
			processQueue(err, null);
			useAuthStore().logout();
			return Promise.reject(err);
		} finally {
			isRefreshing = false;
		}
	}
	return Promise.reject(error);
});
//#endregion
//#region src/stores/auth.js
function loadJson(key, fallback) {
	if (typeof window === "undefined") return fallback;
	try {
		const raw = localStorage.getItem(key);
		return raw ? JSON.parse(raw) : fallback;
	} catch {
		return fallback;
	}
}
function loadString(key, fallback) {
	if (typeof window === "undefined") return fallback;
	try {
		return localStorage.getItem(key) || fallback;
	} catch {
		return fallback;
	}
}
var useAuthStore = defineStore("auth", () => {
	const accessToken = ref(loadString("access_token", null));
	const refreshToken = ref(loadString("refresh_token", null));
	const user = shallowRef(loadJson("auth_user", null));
	const permissions = shallowRef(loadJson("auth_permissions", []));
	const isAuthenticated = computed(() => !!accessToken.value);
	async function fetchMe() {
		try {
			const me = (await api.get("/api/v1/auth/me")).data.data;
			user.value = {
				id: me.id,
				username: me.username,
				fullname: me.fullname,
				roles: me.roles ?? []
			};
			permissions.value = me.permissions ?? [];
			localStorage.setItem("auth_user", JSON.stringify(user.value));
			localStorage.setItem("auth_permissions", JSON.stringify(permissions.value));
		} catch (_) {}
	}
	async function login(username, password) {
		const { accessToken: at, refreshToken: rt } = (await api.post("/api/v1/auth/login", {
			username,
			password
		})).data.data;
		accessToken.value = at;
		refreshToken.value = rt;
		localStorage.setItem("access_token", at);
		localStorage.setItem("refresh_token", rt);
		await fetchMe();
	}
	async function logout() {
		try {
			if (refreshToken.value) await api.post(`/api/v1/auth/logout?refreshToken=${refreshToken.value}`);
		} catch (_) {} finally {
			accessToken.value = null;
			refreshToken.value = null;
			user.value = null;
			permissions.value = [];
			localStorage.removeItem("access_token");
			localStorage.removeItem("refresh_token");
			localStorage.removeItem("auth_user");
			localStorage.removeItem("auth_permissions");
			if (typeof window !== "undefined") window.location.href = "/login";
		}
	}
	return {
		accessToken,
		refreshToken,
		user,
		permissions,
		isAuthenticated,
		login,
		logout,
		fetchMe
	};
});
//#endregion
//#region src/router/index.js
var routes = [
	{
		path: "/",
		name: "landing",
		component: () => import("./assets/LandingPage-BvzztxoD.js")
	},
	{
		path: "/login",
		name: "login",
		component: () => import("./assets/LoginPage-DeYHhC1t.js"),
		meta: { guest: true }
	},
	{
		path: "/dashboard",
		name: "dashboard",
		component: () => import("./assets/DashboardPage-aX4FQNJl.js"),
		meta: {
			requiresAuth: true,
			pageTitle: "Dashboard",
			pageSubtitle: "Ringkasan data dan aktivitas aplikasi."
		}
	},
	{
		path: "/dashboard/products",
		name: "products",
		component: () => import("./assets/ProductPage-DnZyXJ9B.js"),
		meta: {
			requiresAuth: true,
			permission: "produk.index",
			pageTitle: "Manajemen Produk",
			pageSubtitle: "Kelola produk, status, dan kategori."
		}
	},
	{
		path: "/dashboard/categories",
		name: "categories",
		component: () => import("./assets/CategoriesPage-CYzYpdfZ.js"),
		meta: {
			requiresAuth: true,
			permission: "category.index",
			pageTitle: "Manajemen Kategori",
			pageSubtitle: "Atur kategori untuk klasifikasi konten post."
		}
	},
	{
		path: "/dashboard/stock-mutations",
		name: "stock-mutations",
		component: () => import("./assets/StockMutationsPage-CRIwKTFK.js"),
		meta: {
			requiresAuth: true,
			permission: "stock-mutation.index",
			pageTitle: "Mutasi Stok",
			pageSubtitle: "Pantau riwayat pergerakan stok barang."
		}
	},
	{
		path: "/dashboard/partners",
		name: "partners",
		component: () => import("./assets/PartnersPage-QWtySzE1.js"),
		meta: {
			requiresAuth: true,
			permission: "partner.index",
			pageTitle: "Manajemen Partner",
			pageSubtitle: "Kelola data supplier dan customer."
		}
	},
	{
		path: "/dashboard/locations",
		name: "locations",
		component: () => import("./assets/LocationsPage-CVQHD6Tm.js"),
		meta: {
			requiresAuth: true,
			permission: "location.index",
			pageTitle: "Manajemen Lokasi",
			pageSubtitle: "Atur data gudang dan cabang."
		}
	},
	{
		path: "/dashboard/vouchers",
		name: "vouchers",
		component: () => import("./assets/VouchersPage-DqLlYHB_.js"),
		meta: {
			requiresAuth: true,
			permission: "voucher.index",
			pageTitle: "Manajemen Voucer",
			pageSubtitle: "Kelola kode promo dan diskon."
		}
	},
	{
		path: "/dashboard/roles",
		name: "roles",
		component: () => import("./assets/RolesPage-BU9-mV6I.js"),
		meta: {
			requiresAuth: true,
			permission: "role.index",
			pageTitle: "Manajemen Role",
			pageSubtitle: "Kelola role dan matriks hak akses pengguna."
		}
	},
	{
		path: "/dashboard/permissions",
		name: "permissions",
		component: () => import("./assets/PermissionsPage-BLHcHMLz.js"),
		meta: {
			requiresAuth: true,
			permission: "permission.index",
			pageTitle: "Manajemen Permission",
			pageSubtitle: "Kelola slug permission yang dipakai di otorisasi."
		}
	},
	{
		path: "/dashboard/modules",
		name: "modules",
		component: () => import("./assets/ModulesPage-GQuLyQ44.js"),
		meta: {
			requiresAuth: true,
			permission: "module.index",
			pageTitle: "Manajemen Modul",
			pageSubtitle: "Atur grup modul sebagai header untuk permission."
		}
	},
	{
		path: "/dashboard/users",
		name: "users",
		component: () => import("./assets/UsersPage-BcZb-Gjq.js"),
		meta: {
			requiresAuth: true,
			permission: "user.index",
			pageTitle: "Manajemen User",
			pageSubtitle: "Kelola data user dan assign role."
		}
	},
	{
		path: "/dashboard/profile",
		name: "profile",
		component: () => import("./assets/ProfilePage-D1_IYDU1.js"),
		meta: {
			requiresAuth: true,
			pageTitle: "Profil Saya",
			pageSubtitle: "Lihat profil dan ganti password."
		}
	},
	{
		path: "/dashboard/logs",
		name: "logs",
		component: () => import("./assets/LogsPage-DRFAFUdp.js"),
		meta: {
			requiresAuth: true,
			permission: "log.index",
			pageTitle: "Audit Logs",
			pageSubtitle: "Log aktivitas HTTP pada sistem."
		}
	},
	{
		path: "/dashboard/kasir",
		name: "kasir",
		component: () => import("./assets/KasirPage-DdGA3YSd.js"),
		meta: {
			requiresAuth: true,
			pageTitle: "Kasir",
			pageSubtitle: "Sistem Point of Sale"
		}
	},
	{
		path: "/about",
		name: "about",
		component: () => import("./assets/AboutPage-BA5kQST_.js")
	},
	{
		path: "/:pathMatch(.*)*",
		redirect: "/login"
	}
];
var base = "/_/";
var setupRouterGuards = (router) => {
	router.beforeEach((to, from) => {
		const auth = useAuthStore();
		if (to.meta.requiresAuth && !auth.isAuthenticated) return { name: "login" };
		if (to.meta.permission) {
			const userRoles = auth.user?.roles || [];
			if (!(userRoles.includes("ADMIN") || userRoles.some((r) => r.name === "ADMIN")) && !auth.permissions.includes(to.meta.permission)) return { name: "dashboard" };
		}
		if (to.meta.guest && auth.isAuthenticated) return { name: "dashboard" };
	});
};
setupRouterGuards(createRouter({
	history: createWebHistory(base),
	routes
}));
//#endregion
//#region src/stores/theme.js
var THEMES = {
	default: {
		label: "Default",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "222.2 84% 4.9%",
				"--card": "0 0% 100%",
				"--card-foreground": "222.2 84% 4.9%",
				"--primary": "221.2 83.2% 53.3%",
				"--primary-foreground": "210 40% 98%",
				"--secondary": "210 40% 96.1%",
				"--secondary-foreground": "222.2 47.4% 11.2%",
				"--muted": "210 40% 96.1%",
				"--muted-foreground": "215.4 16.3% 46.9%",
				"--accent": "210 40% 96.1%",
				"--accent-foreground": "222.2 47.4% 11.2%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "210 40% 98%",
				"--border": "214.3 31.8% 91.4%",
				"--input": "214.3 31.8% 91.4%",
				"--ring": "221.2 83.2% 53.3%"
			},
			dark: {
				"--background": "222.2 84% 4.9%",
				"--foreground": "210 40% 98%",
				"--card": "222.2 84% 4.9%",
				"--card-foreground": "210 40% 98%",
				"--primary": "217.2 91.2% 59.8%",
				"--primary-foreground": "222.2 47.4% 11.2%",
				"--secondary": "217.2 32.6% 17.5%",
				"--secondary-foreground": "210 40% 98%",
				"--muted": "217.2 32.6% 17.5%",
				"--muted-foreground": "215 20.2% 65.1%",
				"--accent": "217.2 32.6% 17.5%",
				"--accent-foreground": "210 40% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "210 40% 98%",
				"--border": "217.2 32.6% 17.5%",
				"--input": "217.2 32.6% 17.5%",
				"--ring": "224.3 76.3% 48%"
			}
		}
	},
	green: {
		label: "Green",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "240 10% 3.9%",
				"--card": "0 0% 100%",
				"--card-foreground": "240 10% 3.9%",
				"--primary": "142.1 76.2% 36.3%",
				"--primary-foreground": "355.7 100% 97.3%",
				"--secondary": "240 4.8% 95.9%",
				"--secondary-foreground": "240 5.9% 10%",
				"--muted": "240 4.8% 95.9%",
				"--muted-foreground": "240 3.8% 46.1%",
				"--accent": "240 4.8% 95.9%",
				"--accent-foreground": "240 5.9% 10%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "240 5.9% 90%",
				"--input": "240 5.9% 90%",
				"--ring": "142.1 76.2% 36.3%"
			},
			dark: {
				"--background": "20 14.3% 4.1%",
				"--foreground": "0 0% 95%",
				"--card": "24 9.8% 10%",
				"--card-foreground": "0 0% 95%",
				"--primary": "142.1 70.6% 45.3%",
				"--primary-foreground": "144.9 80.4% 10%",
				"--secondary": "240 3.7% 15.9%",
				"--secondary-foreground": "0 0% 98%",
				"--muted": "0 0% 15%",
				"--muted-foreground": "240 5% 64.9%",
				"--accent": "12 6.5% 15.1%",
				"--accent-foreground": "0 0% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "0 85.7% 97.3%",
				"--border": "240 3.7% 15.9%",
				"--input": "240 3.7% 15.9%",
				"--ring": "142.4 71.8% 29.2%"
			}
		}
	},
	violet: {
		label: "Violet",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "224 71.4% 4.1%",
				"--card": "0 0% 100%",
				"--card-foreground": "224 71.4% 4.1%",
				"--primary": "262.1 83.3% 57.8%",
				"--primary-foreground": "210 20% 98%",
				"--secondary": "220 14.3% 95.9%",
				"--secondary-foreground": "220.9 39.3% 11%",
				"--muted": "220 14.3% 95.9%",
				"--muted-foreground": "220 8.9% 46.1%",
				"--accent": "220 14.3% 95.9%",
				"--accent-foreground": "220.9 39.3% 11%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "210 20% 98%",
				"--border": "220 13% 91%",
				"--input": "220 13% 91%",
				"--ring": "262.1 83.3% 57.8%"
			},
			dark: {
				"--background": "224 71.4% 4.1%",
				"--foreground": "210 20% 98%",
				"--card": "224 71.4% 4.1%",
				"--card-foreground": "210 20% 98%",
				"--primary": "263.4 70% 50.4%",
				"--primary-foreground": "210 20% 98%",
				"--secondary": "215 27.9% 16.9%",
				"--secondary-foreground": "210 20% 98%",
				"--muted": "215 27.9% 16.9%",
				"--muted-foreground": "217.9 10.5% 64.9%",
				"--accent": "215 27.9% 16.9%",
				"--accent-foreground": "210 20% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "210 20% 98%",
				"--border": "215 27.9% 16.9%",
				"--input": "215 27.9% 16.9%",
				"--ring": "263.4 70% 50.4%"
			}
		}
	},
	orange: {
		label: "Orange",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "20 14.3% 4.1%",
				"--card": "0 0% 100%",
				"--card-foreground": "20 14.3% 4.1%",
				"--primary": "24.6 95% 53.1%",
				"--primary-foreground": "60 9.1% 97.8%",
				"--secondary": "60 4.8% 95.9%",
				"--secondary-foreground": "24 9.8% 10%",
				"--muted": "60 4.8% 95.9%",
				"--muted-foreground": "25 5.3% 44.7%",
				"--accent": "60 4.8% 95.9%",
				"--accent-foreground": "24 9.8% 10%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "60 9.1% 97.8%",
				"--border": "20 5.9% 90%",
				"--input": "20 5.9% 90%",
				"--ring": "24.6 95% 53.1%"
			},
			dark: {
				"--background": "20 14.3% 4.1%",
				"--foreground": "60 9.1% 97.8%",
				"--card": "20 14.3% 4.1%",
				"--card-foreground": "60 9.1% 97.8%",
				"--primary": "20.5 90.2% 48.2%",
				"--primary-foreground": "60 9.1% 97.8%",
				"--secondary": "12 6.5% 15.1%",
				"--secondary-foreground": "60 9.1% 97.8%",
				"--muted": "12 6.5% 15.1%",
				"--muted-foreground": "24 5.4% 63.9%",
				"--accent": "12 6.5% 15.1%",
				"--accent-foreground": "60 9.1% 97.8%",
				"--destructive": "0 72.2% 50.6%",
				"--destructive-foreground": "60 9.1% 97.8%",
				"--border": "12 6.5% 15.1%",
				"--input": "12 6.5% 15.1%",
				"--ring": "20.5 90.2% 48.2%"
			}
		}
	},
	rose: {
		label: "Rose",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "240 10% 3.9%",
				"--card": "0 0% 100%",
				"--card-foreground": "240 10% 3.9%",
				"--primary": "346.8 77.2% 49.8%",
				"--primary-foreground": "355.7 100% 97.3%",
				"--secondary": "240 4.8% 95.9%",
				"--secondary-foreground": "240 5.9% 10%",
				"--muted": "240 4.8% 95.9%",
				"--muted-foreground": "240 3.8% 46.1%",
				"--accent": "240 4.8% 95.9%",
				"--accent-foreground": "240 5.9% 10%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "240 5.9% 90%",
				"--input": "240 5.9% 90%",
				"--ring": "346.8 77.2% 49.8%"
			},
			dark: {
				"--background": "20 14.3% 4.1%",
				"--foreground": "0 0% 95%",
				"--card": "24 9.8% 10%",
				"--card-foreground": "0 0% 95%",
				"--primary": "346.8 77.2% 49.8%",
				"--primary-foreground": "355.7 100% 97.3%",
				"--secondary": "240 3.7% 15.9%",
				"--secondary-foreground": "0 0% 98%",
				"--muted": "0 0% 15%",
				"--muted-foreground": "240 5% 64.9%",
				"--accent": "12 6.5% 15.1%",
				"--accent-foreground": "0 0% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "0 85.7% 97.3%",
				"--border": "240 3.7% 15.9%",
				"--input": "240 3.7% 15.9%",
				"--ring": "346.8 77.2% 49.8%"
			}
		}
	},
	yellow: {
		label: "Yellow",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "20 14.3% 4.1%",
				"--card": "0 0% 100%",
				"--card-foreground": "20 14.3% 4.1%",
				"--primary": "47.9 95.8% 53.1%",
				"--primary-foreground": "26 83.3% 14.1%",
				"--secondary": "60 4.8% 95.9%",
				"--secondary-foreground": "24 9.8% 10%",
				"--muted": "60 4.8% 95.9%",
				"--muted-foreground": "25 5.3% 44.7%",
				"--accent": "60 4.8% 95.9%",
				"--accent-foreground": "24 9.8% 10%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "60 9.1% 97.8%",
				"--border": "20 5.9% 90%",
				"--input": "20 5.9% 90%",
				"--ring": "20 14.3% 4.1%"
			},
			dark: {
				"--background": "20 14.3% 4.1%",
				"--foreground": "60 9.1% 97.8%",
				"--card": "20 14.3% 4.1%",
				"--card-foreground": "60 9.1% 97.8%",
				"--primary": "47.9 95.8% 53.1%",
				"--primary-foreground": "26 83.3% 14.1%",
				"--secondary": "12 6.5% 15.1%",
				"--secondary-foreground": "60 9.1% 97.8%",
				"--muted": "12 6.5% 15.1%",
				"--muted-foreground": "24 5.4% 63.9%",
				"--accent": "12 6.5% 15.1%",
				"--accent-foreground": "60 9.1% 97.8%",
				"--destructive": "0 72.2% 50.6%",
				"--destructive-foreground": "60 9.1% 97.8%",
				"--border": "12 6.5% 15.1%",
				"--input": "12 6.5% 15.1%",
				"--ring": "35.5 91.7% 32.9%"
			}
		}
	},
	zinc: {
		label: "Zinc",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "240 10% 3.9%",
				"--card": "0 0% 100%",
				"--card-foreground": "240 10% 3.9%",
				"--primary": "240 5.9% 10%",
				"--primary-foreground": "0 0% 98%",
				"--secondary": "240 4.8% 95.9%",
				"--secondary-foreground": "240 5.9% 10%",
				"--muted": "240 4.8% 95.9%",
				"--muted-foreground": "240 3.8% 46.1%",
				"--accent": "240 4.8% 95.9%",
				"--accent-foreground": "240 5.9% 10%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "240 5.9% 90%",
				"--input": "240 5.9% 90%",
				"--ring": "240 5.9% 10%"
			},
			dark: {
				"--background": "240 10% 3.9%",
				"--foreground": "0 0% 98%",
				"--card": "240 10% 3.9%",
				"--card-foreground": "0 0% 98%",
				"--primary": "0 0% 98%",
				"--primary-foreground": "240 5.9% 10%",
				"--secondary": "240 3.7% 15.9%",
				"--secondary-foreground": "0 0% 98%",
				"--muted": "240 3.7% 15.9%",
				"--muted-foreground": "240 5% 64.9%",
				"--accent": "240 3.7% 15.9%",
				"--accent-foreground": "0 0% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "240 3.7% 15.9%",
				"--input": "240 3.7% 15.9%",
				"--ring": "240 4.9% 83.9%"
			}
		}
	},
	red: {
		label: "Red",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "0 0% 3.9%",
				"--card": "0 0% 100%",
				"--card-foreground": "0 0% 3.9%",
				"--primary": "0 72.2% 50.6%",
				"--primary-foreground": "0 85.7% 97.3%",
				"--secondary": "0 0% 96.1%",
				"--secondary-foreground": "0 0% 9%",
				"--muted": "0 0% 96.1%",
				"--muted-foreground": "0 0% 45.1%",
				"--accent": "0 0% 96.1%",
				"--accent-foreground": "0 0% 9%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "0 0% 89.8%",
				"--input": "0 0% 89.8%",
				"--ring": "0 72.2% 50.6%"
			},
			dark: {
				"--background": "0 0% 3.9%",
				"--foreground": "0 0% 98%",
				"--card": "0 0% 3.9%",
				"--card-foreground": "0 0% 98%",
				"--primary": "0 72.2% 50.6%",
				"--primary-foreground": "0 85.7% 97.3%",
				"--secondary": "0 0% 14.9%",
				"--secondary-foreground": "0 0% 98%",
				"--muted": "0 0% 14.9%",
				"--muted-foreground": "0 0% 63.9%",
				"--accent": "0 0% 14.9%",
				"--accent-foreground": "0 0% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "0 0% 14.9%",
				"--input": "0 0% 14.9%",
				"--ring": "0 72.2% 50.6%"
			}
		}
	},
	teal: {
		label: "Teal",
		colors: {
			light: {
				"--background": "0 0% 100%",
				"--foreground": "20 14.3% 4.1%",
				"--card": "0 0% 100%",
				"--card-foreground": "20 14.3% 4.1%",
				"--primary": "171 100% 29%",
				"--primary-foreground": "0 0% 100%",
				"--secondary": "60 4.8% 95.9%",
				"--secondary-foreground": "24 9.8% 10%",
				"--muted": "60 4.8% 95.9%",
				"--muted-foreground": "25 5.3% 44.7%",
				"--accent": "60 4.8% 95.9%",
				"--accent-foreground": "24 9.8% 10%",
				"--destructive": "0 84.2% 60.2%",
				"--destructive-foreground": "60 9.1% 97.8%",
				"--border": "20 5.9% 90%",
				"--input": "20 5.9% 90%",
				"--ring": "171 100% 29%"
			},
			dark: {
				"--background": "20 14.3% 4.1%",
				"--foreground": "0 0% 98%",
				"--card": "20 14.3% 4.1%",
				"--card-foreground": "0 0% 98%",
				"--primary": "171 77% 41%",
				"--primary-foreground": "0 0% 100%",
				"--secondary": "12 6.5% 15.1%",
				"--secondary-foreground": "0 0% 98%",
				"--muted": "12 6.5% 15.1%",
				"--muted-foreground": "24 5.4% 63.9%",
				"--accent": "12 6.5% 15.1%",
				"--accent-foreground": "0 0% 98%",
				"--destructive": "0 62.8% 50.6%",
				"--destructive-foreground": "0 0% 98%",
				"--border": "12 6.5% 15.1%",
				"--input": "12 6.5% 15.1%",
				"--ring": "171 77% 41%"
			}
		}
	}
};
var useThemeStore = defineStore("theme", () => {
	const currentTheme = ref(typeof window !== "undefined" && localStorage.getItem("theme") ? localStorage.getItem("theme") : "zinc");
	const isDark = ref(false);
	function init() {
		const savedDark = localStorage.getItem("isDark");
		if (savedDark !== null) isDark.value = savedDark === "true";
		else isDark.value = window.matchMedia("(prefers-color-scheme: dark)").matches;
		applyTheme(currentTheme.value, isDark.value);
		window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", (e) => {
			if (localStorage.getItem("isDark") === null) applyTheme(currentTheme.value, e.matches);
		});
	}
	const themeLabels = computed(() => {
		return Object.entries(THEMES).map(([key, value]) => ({
			key,
			label: value.label,
			color: `hsl(${value.colors.light["--primary"]})`
		}));
	});
	function applyTheme(themeKey, dark) {
		const theme = THEMES[themeKey];
		if (!theme) return;
		const colorScheme = dark ? "dark" : "light";
		const colors = theme.colors[colorScheme];
		const root = document.documentElement;
		Object.entries(colors).forEach(([prop, value]) => {
			root.style.setProperty(prop, value);
		});
		if (dark) root.classList.add("dark");
		else root.classList.remove("dark");
		localStorage.setItem("theme", themeKey);
		localStorage.setItem("isDark", String(dark));
		currentTheme.value = themeKey;
		isDark.value = dark;
	}
	function toggleDark() {
		applyTheme(currentTheme.value, !isDark.value);
	}
	function setTheme(themeKey) {
		applyTheme(themeKey, isDark.value);
	}
	return {
		currentTheme,
		isDark,
		themeLabels,
		THEMES,
		applyTheme,
		toggleDark,
		setTheme,
		init
	};
});
//#endregion
//#region src/main.js
var createApp = ViteSSG(_sfc_main, {
	routes,
	base: "/_/"
}, ({ app, router, isClient }) => {
	const pinia = createPinia();
	app.use(pinia);
	if (isClient) {
		setupRouterGuards(router);
		useThemeStore().init();
	}
});
//#endregion
export { createApp, useNotification as i, useAuthStore as n, api as r, useThemeStore as t };
