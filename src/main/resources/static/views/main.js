define(function (require) {
	var domReady = require("domReady");
	var	vue = require("vue");
	var	VueRouter = require("vue-router");
	var Vuex = require("vuex");
	var	VueResource = require("vue-resource");
	var Element = require("ELEMENT");
	vue.use(VueRouter);
	vue.use(Vuex);
	vue.use(VueResource);
	vue.use(Element);
	
	domReady(function() {
		new vue({
			el: '#app',
			router: new VueRouter({
				routes: [
					{
						path: '/',
						redirect: '/datasource/list'
					},
					{
						path: '/datasource/list',
						component: require('/views/components/datasource/list/datasource-list.js')
					},
					{
						path: '/datasource/add',
						component: require('/views/components/datasource/add/datasource-add.js')
					},
					{
						path: '/datasource/edit/:type/:id?',
						component: require('/views/components/datasource/edit/datasource-edit.js')
					},
					{
						path: '/task/list',
						component: require('/views/components/task/list/task-list.js')
					},
					{
						path: '/task/edit/:id?',
						component: require('/views/components/task/edit/task-edit.js')
					}
				]
			}),
			computed: {
				isDatasourceMenuActive() {
					return this.$route.path.startsWith('/datasource');
				},
				isTaskMenuActive() {
					return this.$route.path.startsWith('/task');
				}
			}
		});
	})
})