import { n as useAuthStore, r as api } from "../main.mjs";
import { t as _sfc_main$1 } from "./Button-Bj0EF1Kv.js";
import { t as _sfc_main$2 } from "./AppLayout-D1IhsFmL.js";
import { t as useToast } from "./useToast-BeMK7Zjj.js";
import { t as _sfc_main$3 } from "./Card-ClMbbMGU.js";
import { t as _sfc_main$4 } from "./CardContent-g9O7qVnh.js";
import { t as _sfc_main$5 } from "./Input-yu8tAo3O.js";
import { n as _sfc_main$7, t as _sfc_main$6 } from "./Alert-DMYknBO3.js";
import { t as _sfc_main$8 } from "./Badge-PdtEYXOU.js";
import { n as _sfc_main$9, t as _sfc_main$10 } from "./CardTitle-CWxkLjm1.js";
import { Fragment, createBlock, createCommentVNode, createTextVNode, createVNode, openBlock, ref, renderList, toDisplayString, unref, useSSRContext, withCtx } from "vue";
import { ssrInterpolate, ssrRenderComponent, ssrRenderList } from "vue/server-renderer";
import { Loader2, ShieldCheck } from "lucide-vue-next";
//#region src/pages/ProfilePage.vue
var _sfc_main = {
	__name: "ProfilePage",
	__ssrInlineRender: true,
	setup(__props) {
		const auth = useAuthStore();
		const { toast } = useToast();
		const saving = ref(false);
		const formError = ref(null);
		const form = ref({
			currentPassword: "",
			newPassword: "",
			confirmNewPassword: ""
		});
		async function changePassword() {
			formError.value = null;
			if (!form.value.currentPassword) {
				formError.value = "Current password is required";
				return;
			}
			if (!form.value.newPassword || form.value.newPassword.length < 6) {
				formError.value = "New password must be at least 6 characters";
				return;
			}
			if (form.value.newPassword !== form.value.confirmNewPassword) {
				formError.value = "New password and confirmation do not match";
				return;
			}
			saving.value = true;
			try {
				await api.put("/api/v1/users/me/password", {
					currentPassword: form.value.currentPassword,
					newPassword: form.value.newPassword
				});
				toast.success("Password changed successfully!");
				form.value = {
					currentPassword: "",
					newPassword: "",
					confirmNewPassword: ""
				};
			} catch (err) {
				formError.value = err.response?.data?.data?.message || err.response?.data?.message || "Failed to change password. Check your current password.";
			} finally {
				saving.value = false;
			}
		}
		function getRoleBadges() {
			return auth.user?.roles || [];
		}
		return (_ctx, _push, _parent, _attrs) => {
			_push(ssrRenderComponent(_sfc_main$2, _attrs, {
				default: withCtx((_, _push, _parent, _scopeId) => {
					if (_push) {
						if (formError.value) _push(ssrRenderComponent(_sfc_main$6, {
							variant: "destructive",
							class: "mb-4"
						}, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) _push(`${ssrInterpolate(formError.value)}`);
								else return [createTextVNode(toDisplayString(formError.value), 1)];
							}),
							_: 1
						}, _parent, _scopeId));
						else _push(`<!---->`);
						_push(`<div class="grid gap-6 max-w-2xl"${_scopeId}>`);
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(_sfc_main$9, null, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) _push(ssrRenderComponent(_sfc_main$10, null, {
												default: withCtx((_, _push, _parent, _scopeId) => {
													if (_push) _push(`Profil Saya`);
													else return [createTextVNode("Profil Saya")];
												}),
												_: 1
											}, _parent, _scopeId));
											else return [createVNode(_sfc_main$10, null, {
												default: withCtx(() => [createTextVNode("Profil Saya")]),
												_: 1
											})];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(_sfc_main$4, { class: "space-y-4" }, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) {
												_push(`<div class="grid grid-cols-2 gap-4 text-sm"${_scopeId}><div${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$7, { class: "text-muted-foreground" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`Username`);
														else return [createTextVNode("Username")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(`<p class="font-medium mt-1"${_scopeId}>${ssrInterpolate(unref(auth).user?.username || "-")}</p></div><div${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$7, { class: "text-muted-foreground" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`Full Name`);
														else return [createTextVNode("Full Name")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(`<p class="font-medium mt-1"${_scopeId}>${ssrInterpolate(unref(auth).user?.fullname || "-")}</p></div></div><div${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$7, { class: "text-muted-foreground" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`Roles`);
														else return [createTextVNode("Roles")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(`<div class="flex flex-wrap gap-1 mt-1"${_scopeId}><!--[-->`);
												ssrRenderList(getRoleBadges(), (role) => {
													_push(ssrRenderComponent(_sfc_main$8, {
														key: role,
														variant: "secondary",
														class: "text-xs"
													}, {
														default: withCtx((_, _push, _parent, _scopeId) => {
															if (_push) {
																_push(ssrRenderComponent(unref(ShieldCheck), { class: "h-3 w-3 mr-1" }, null, _parent, _scopeId));
																_push(` ${ssrInterpolate(role)}`);
															} else return [createVNode(unref(ShieldCheck), { class: "h-3 w-3 mr-1" }), createTextVNode(" " + toDisplayString(role), 1)];
														}),
														_: 2
													}, _parent, _scopeId));
												});
												_push(`<!--]-->`);
												if (!getRoleBadges().length) _push(`<span class="text-muted-foreground"${_scopeId}>-</span>`);
												else _push(`<!---->`);
												_push(`</div></div>`);
											} else return [createVNode("div", { class: "grid grid-cols-2 gap-4 text-sm" }, [createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
												default: withCtx(() => [createTextVNode("Username")]),
												_: 1
											}), createVNode("p", { class: "font-medium mt-1" }, toDisplayString(unref(auth).user?.username || "-"), 1)]), createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
												default: withCtx(() => [createTextVNode("Full Name")]),
												_: 1
											}), createVNode("p", { class: "font-medium mt-1" }, toDisplayString(unref(auth).user?.fullname || "-"), 1)])]), createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
												default: withCtx(() => [createTextVNode("Roles")]),
												_: 1
											}), createVNode("div", { class: "flex flex-wrap gap-1 mt-1" }, [(openBlock(true), createBlock(Fragment, null, renderList(getRoleBadges(), (role) => {
												return openBlock(), createBlock(_sfc_main$8, {
													key: role,
													variant: "secondary",
													class: "text-xs"
												}, {
													default: withCtx(() => [createVNode(unref(ShieldCheck), { class: "h-3 w-3 mr-1" }), createTextVNode(" " + toDisplayString(role), 1)]),
													_: 2
												}, 1024);
											}), 128)), !getRoleBadges().length ? (openBlock(), createBlock("span", {
												key: 0,
												class: "text-muted-foreground"
											}, "-")) : createCommentVNode("", true)])])];
										}),
										_: 1
									}, _parent, _scopeId));
								} else return [createVNode(_sfc_main$9, null, {
									default: withCtx(() => [createVNode(_sfc_main$10, null, {
										default: withCtx(() => [createTextVNode("Profil Saya")]),
										_: 1
									})]),
									_: 1
								}), createVNode(_sfc_main$4, { class: "space-y-4" }, {
									default: withCtx(() => [createVNode("div", { class: "grid grid-cols-2 gap-4 text-sm" }, [createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
										default: withCtx(() => [createTextVNode("Username")]),
										_: 1
									}), createVNode("p", { class: "font-medium mt-1" }, toDisplayString(unref(auth).user?.username || "-"), 1)]), createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
										default: withCtx(() => [createTextVNode("Full Name")]),
										_: 1
									}), createVNode("p", { class: "font-medium mt-1" }, toDisplayString(unref(auth).user?.fullname || "-"), 1)])]), createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
										default: withCtx(() => [createTextVNode("Roles")]),
										_: 1
									}), createVNode("div", { class: "flex flex-wrap gap-1 mt-1" }, [(openBlock(true), createBlock(Fragment, null, renderList(getRoleBadges(), (role) => {
										return openBlock(), createBlock(_sfc_main$8, {
											key: role,
											variant: "secondary",
											class: "text-xs"
										}, {
											default: withCtx(() => [createVNode(unref(ShieldCheck), { class: "h-3 w-3 mr-1" }), createTextVNode(" " + toDisplayString(role), 1)]),
											_: 2
										}, 1024);
									}), 128)), !getRoleBadges().length ? (openBlock(), createBlock("span", {
										key: 0,
										class: "text-muted-foreground"
									}, "-")) : createCommentVNode("", true)])])]),
									_: 1
								})];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(ssrRenderComponent(_sfc_main$3, null, {
							default: withCtx((_, _push, _parent, _scopeId) => {
								if (_push) {
									_push(ssrRenderComponent(_sfc_main$9, null, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) _push(ssrRenderComponent(_sfc_main$10, null, {
												default: withCtx((_, _push, _parent, _scopeId) => {
													if (_push) _push(`Ganti Password`);
													else return [createTextVNode("Ganti Password")];
												}),
												_: 1
											}, _parent, _scopeId));
											else return [createVNode(_sfc_main$10, null, {
												default: withCtx(() => [createTextVNode("Ganti Password")]),
												_: 1
											})];
										}),
										_: 1
									}, _parent, _scopeId));
									_push(ssrRenderComponent(_sfc_main$4, { class: "space-y-4" }, {
										default: withCtx((_, _push, _parent, _scopeId) => {
											if (_push) {
												_push(`<div class="space-y-2"${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$7, { for: "p-current" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`Current Password <span class="text-destructive"${_scopeId}>*</span>`);
														else return [createTextVNode("Current Password "), createVNode("span", { class: "text-destructive" }, "*")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(ssrRenderComponent(_sfc_main$5, {
													id: "p-current",
													modelValue: form.value.currentPassword,
													"onUpdate:modelValue": ($event) => form.value.currentPassword = $event,
													type: "password",
													placeholder: "Current password",
													disabled: saving.value
												}, null, _parent, _scopeId));
												_push(`</div><div class="space-y-2"${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$7, { for: "p-new" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`New Password <span class="text-destructive"${_scopeId}>*</span>`);
														else return [createTextVNode("New Password "), createVNode("span", { class: "text-destructive" }, "*")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(ssrRenderComponent(_sfc_main$5, {
													id: "p-new",
													modelValue: form.value.newPassword,
													"onUpdate:modelValue": ($event) => form.value.newPassword = $event,
													type: "password",
													placeholder: "New password (min 6 chars)",
													disabled: saving.value
												}, null, _parent, _scopeId));
												_push(`</div><div class="space-y-2"${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$7, { for: "p-confirm" }, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) _push(`Confirm New Password <span class="text-destructive"${_scopeId}>*</span>`);
														else return [createTextVNode("Confirm New Password "), createVNode("span", { class: "text-destructive" }, "*")];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(ssrRenderComponent(_sfc_main$5, {
													id: "p-confirm",
													modelValue: form.value.confirmNewPassword,
													"onUpdate:modelValue": ($event) => form.value.confirmNewPassword = $event,
													type: "password",
													placeholder: "Confirm new password",
													disabled: saving.value
												}, null, _parent, _scopeId));
												_push(`</div><div class="flex justify-end pt-2"${_scopeId}>`);
												_push(ssrRenderComponent(_sfc_main$1, {
													onClick: changePassword,
													disabled: saving.value
												}, {
													default: withCtx((_, _push, _parent, _scopeId) => {
														if (_push) {
															if (saving.value) _push(ssrRenderComponent(unref(Loader2), { class: "h-4 w-4 mr-2 animate-spin" }, null, _parent, _scopeId));
															else _push(`<!---->`);
															_push(` ${ssrInterpolate(saving.value ? "Saving..." : "Change Password")}`);
														} else return [saving.value ? (openBlock(), createBlock(unref(Loader2), {
															key: 0,
															class: "h-4 w-4 mr-2 animate-spin"
														})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(saving.value ? "Saving..." : "Change Password"), 1)];
													}),
													_: 1
												}, _parent, _scopeId));
												_push(`</div>`);
											} else return [
												createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-current" }, {
													default: withCtx(() => [createTextVNode("Current Password "), createVNode("span", { class: "text-destructive" }, "*")]),
													_: 1
												}), createVNode(_sfc_main$5, {
													id: "p-current",
													modelValue: form.value.currentPassword,
													"onUpdate:modelValue": ($event) => form.value.currentPassword = $event,
													type: "password",
													placeholder: "Current password",
													disabled: saving.value
												}, null, 8, [
													"modelValue",
													"onUpdate:modelValue",
													"disabled"
												])]),
												createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-new" }, {
													default: withCtx(() => [createTextVNode("New Password "), createVNode("span", { class: "text-destructive" }, "*")]),
													_: 1
												}), createVNode(_sfc_main$5, {
													id: "p-new",
													modelValue: form.value.newPassword,
													"onUpdate:modelValue": ($event) => form.value.newPassword = $event,
													type: "password",
													placeholder: "New password (min 6 chars)",
													disabled: saving.value
												}, null, 8, [
													"modelValue",
													"onUpdate:modelValue",
													"disabled"
												])]),
												createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-confirm" }, {
													default: withCtx(() => [createTextVNode("Confirm New Password "), createVNode("span", { class: "text-destructive" }, "*")]),
													_: 1
												}), createVNode(_sfc_main$5, {
													id: "p-confirm",
													modelValue: form.value.confirmNewPassword,
													"onUpdate:modelValue": ($event) => form.value.confirmNewPassword = $event,
													type: "password",
													placeholder: "Confirm new password",
													disabled: saving.value
												}, null, 8, [
													"modelValue",
													"onUpdate:modelValue",
													"disabled"
												])]),
												createVNode("div", { class: "flex justify-end pt-2" }, [createVNode(_sfc_main$1, {
													onClick: changePassword,
													disabled: saving.value
												}, {
													default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
														key: 0,
														class: "h-4 w-4 mr-2 animate-spin"
													})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(saving.value ? "Saving..." : "Change Password"), 1)]),
													_: 1
												}, 8, ["disabled"])])
											];
										}),
										_: 1
									}, _parent, _scopeId));
								} else return [createVNode(_sfc_main$9, null, {
									default: withCtx(() => [createVNode(_sfc_main$10, null, {
										default: withCtx(() => [createTextVNode("Ganti Password")]),
										_: 1
									})]),
									_: 1
								}), createVNode(_sfc_main$4, { class: "space-y-4" }, {
									default: withCtx(() => [
										createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-current" }, {
											default: withCtx(() => [createTextVNode("Current Password "), createVNode("span", { class: "text-destructive" }, "*")]),
											_: 1
										}), createVNode(_sfc_main$5, {
											id: "p-current",
											modelValue: form.value.currentPassword,
											"onUpdate:modelValue": ($event) => form.value.currentPassword = $event,
											type: "password",
											placeholder: "Current password",
											disabled: saving.value
										}, null, 8, [
											"modelValue",
											"onUpdate:modelValue",
											"disabled"
										])]),
										createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-new" }, {
											default: withCtx(() => [createTextVNode("New Password "), createVNode("span", { class: "text-destructive" }, "*")]),
											_: 1
										}), createVNode(_sfc_main$5, {
											id: "p-new",
											modelValue: form.value.newPassword,
											"onUpdate:modelValue": ($event) => form.value.newPassword = $event,
											type: "password",
											placeholder: "New password (min 6 chars)",
											disabled: saving.value
										}, null, 8, [
											"modelValue",
											"onUpdate:modelValue",
											"disabled"
										])]),
										createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-confirm" }, {
											default: withCtx(() => [createTextVNode("Confirm New Password "), createVNode("span", { class: "text-destructive" }, "*")]),
											_: 1
										}), createVNode(_sfc_main$5, {
											id: "p-confirm",
											modelValue: form.value.confirmNewPassword,
											"onUpdate:modelValue": ($event) => form.value.confirmNewPassword = $event,
											type: "password",
											placeholder: "Confirm new password",
											disabled: saving.value
										}, null, 8, [
											"modelValue",
											"onUpdate:modelValue",
											"disabled"
										])]),
										createVNode("div", { class: "flex justify-end pt-2" }, [createVNode(_sfc_main$1, {
											onClick: changePassword,
											disabled: saving.value
										}, {
											default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
												key: 0,
												class: "h-4 w-4 mr-2 animate-spin"
											})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(saving.value ? "Saving..." : "Change Password"), 1)]),
											_: 1
										}, 8, ["disabled"])])
									]),
									_: 1
								})];
							}),
							_: 1
						}, _parent, _scopeId));
						_push(`</div>`);
					} else return [formError.value ? (openBlock(), createBlock(_sfc_main$6, {
						key: 0,
						variant: "destructive",
						class: "mb-4"
					}, {
						default: withCtx(() => [createTextVNode(toDisplayString(formError.value), 1)]),
						_: 1
					})) : createCommentVNode("", true), createVNode("div", { class: "grid gap-6 max-w-2xl" }, [createVNode(_sfc_main$3, null, {
						default: withCtx(() => [createVNode(_sfc_main$9, null, {
							default: withCtx(() => [createVNode(_sfc_main$10, null, {
								default: withCtx(() => [createTextVNode("Profil Saya")]),
								_: 1
							})]),
							_: 1
						}), createVNode(_sfc_main$4, { class: "space-y-4" }, {
							default: withCtx(() => [createVNode("div", { class: "grid grid-cols-2 gap-4 text-sm" }, [createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
								default: withCtx(() => [createTextVNode("Username")]),
								_: 1
							}), createVNode("p", { class: "font-medium mt-1" }, toDisplayString(unref(auth).user?.username || "-"), 1)]), createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
								default: withCtx(() => [createTextVNode("Full Name")]),
								_: 1
							}), createVNode("p", { class: "font-medium mt-1" }, toDisplayString(unref(auth).user?.fullname || "-"), 1)])]), createVNode("div", null, [createVNode(_sfc_main$7, { class: "text-muted-foreground" }, {
								default: withCtx(() => [createTextVNode("Roles")]),
								_: 1
							}), createVNode("div", { class: "flex flex-wrap gap-1 mt-1" }, [(openBlock(true), createBlock(Fragment, null, renderList(getRoleBadges(), (role) => {
								return openBlock(), createBlock(_sfc_main$8, {
									key: role,
									variant: "secondary",
									class: "text-xs"
								}, {
									default: withCtx(() => [createVNode(unref(ShieldCheck), { class: "h-3 w-3 mr-1" }), createTextVNode(" " + toDisplayString(role), 1)]),
									_: 2
								}, 1024);
							}), 128)), !getRoleBadges().length ? (openBlock(), createBlock("span", {
								key: 0,
								class: "text-muted-foreground"
							}, "-")) : createCommentVNode("", true)])])]),
							_: 1
						})]),
						_: 1
					}), createVNode(_sfc_main$3, null, {
						default: withCtx(() => [createVNode(_sfc_main$9, null, {
							default: withCtx(() => [createVNode(_sfc_main$10, null, {
								default: withCtx(() => [createTextVNode("Ganti Password")]),
								_: 1
							})]),
							_: 1
						}), createVNode(_sfc_main$4, { class: "space-y-4" }, {
							default: withCtx(() => [
								createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-current" }, {
									default: withCtx(() => [createTextVNode("Current Password "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "p-current",
									modelValue: form.value.currentPassword,
									"onUpdate:modelValue": ($event) => form.value.currentPassword = $event,
									type: "password",
									placeholder: "Current password",
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])]),
								createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-new" }, {
									default: withCtx(() => [createTextVNode("New Password "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "p-new",
									modelValue: form.value.newPassword,
									"onUpdate:modelValue": ($event) => form.value.newPassword = $event,
									type: "password",
									placeholder: "New password (min 6 chars)",
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])]),
								createVNode("div", { class: "space-y-2" }, [createVNode(_sfc_main$7, { for: "p-confirm" }, {
									default: withCtx(() => [createTextVNode("Confirm New Password "), createVNode("span", { class: "text-destructive" }, "*")]),
									_: 1
								}), createVNode(_sfc_main$5, {
									id: "p-confirm",
									modelValue: form.value.confirmNewPassword,
									"onUpdate:modelValue": ($event) => form.value.confirmNewPassword = $event,
									type: "password",
									placeholder: "Confirm new password",
									disabled: saving.value
								}, null, 8, [
									"modelValue",
									"onUpdate:modelValue",
									"disabled"
								])]),
								createVNode("div", { class: "flex justify-end pt-2" }, [createVNode(_sfc_main$1, {
									onClick: changePassword,
									disabled: saving.value
								}, {
									default: withCtx(() => [saving.value ? (openBlock(), createBlock(unref(Loader2), {
										key: 0,
										class: "h-4 w-4 mr-2 animate-spin"
									})) : createCommentVNode("", true), createTextVNode(" " + toDisplayString(saving.value ? "Saving..." : "Change Password"), 1)]),
									_: 1
								}, 8, ["disabled"])])
							]),
							_: 1
						})]),
						_: 1
					})])];
				}),
				_: 1
			}, _parent));
		};
	}
};
var _sfc_setup = _sfc_main.setup;
_sfc_main.setup = (props, ctx) => {
	const ssrContext = useSSRContext();
	(ssrContext.modules || (ssrContext.modules = /* @__PURE__ */ new Set())).add("src/pages/ProfilePage.vue");
	return _sfc_setup ? _sfc_setup(props, ctx) : void 0;
};
//#endregion
export { _sfc_main as default };
