define(function(require){
	var vue = require("vue");
	return vue.extend({
		template: require('text!/views/components/datasource/list/datasource-list.html'),
		data () {
			return {
				searchInput: '',
				datasourceList: []
			}
		},
		computed: {
			filteredDatasourceList() {
				const keywords = this.searchInput;
				return this.datasourceList.filter(item => {
				    return (
				        (item.name || '').toLowerCase().includes(keywords) ||
				        (item.description || '').toLowerCase().includes(keywords) ||
				        (item.type || '').toLowerCase().includes(keywords) ||
				        (item.property?.url).toLowerCase().includes(keywords)
				    )
				});
			}
		},
		mounted () {
			this.listDatasource();
		},
		methods: {
			listDatasource() {
				var self = this;
				self.datasourceList = [];
				this.$http.get('/datasource/list')
					.then(response => {
						if (!!response.data) {
							var responseData = response.data;
							self.datasourceList = responseData.data;
							self.datasourceList.forEach(item => {
								item.icon = self.getDatasourceIcon(item.type);
							});
						}
					})
					.catch(error => {
						console.log(error);
					});
			},
			addDatasource() {
				this.$router.push('/datasource/add');
			},
			editDatasource(type, id) {
				this.$router.push(`/datasource/edit/${type}/${id}`);
			},
			getDatasourceIcon(type) {
				if (type === 'MySQL') {
					return '/assets/img/mysql_logo.svg';
				} else if (type === 'SQLServer') {
					return '/assets/img/sql_server_logo.svg';
				} else if (type === 'PostgreSQL') {
					return '/assets/img/postgresql_logo.svg';
				} else if (type === 'Oracle') {
					return '/assets/img/oracle_logo.svg';
				} else {
					return '';
				}
			},
		}
	});
})