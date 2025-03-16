define(function(require){
	var vue = require("vue");
	return vue.extend({
		template: require('text!/views/components/datasource/add/datasource-add.html'),
		data () {
			return {
				searchInput: '',
				datasourceTypeOptions: [
					{
						type: 'MySQL',
						description: 'Data source for MySQL databases',
						icon: '/assets/img/mysql_logo.svg'
					}, 
					{
						type: 'Oracle',
						description: 'Data source for Oracle databases',
						icon: '/assets/img/oracle_logo.svg'
					},
					{
						type: 'PostgreSQL',
						description: 'Data source for PostgreSQL and compatible databases',
						icon: '/assets/img/postgresql_logo.svg'
					},
					{
						type: 'Microsoft SQL Server',
						description: 'Data source for Microsoft SQL Server compatible databases',
						icon: '/assets/img/sql_server_logo.svg'
					}
				]
			}
		},
		computed: {
			filteredDatasourceTypeOptions() {
				const keywords = this.searchInput;
				return this.datasourceTypeOptions.filter(item => {
				    return (
				        (item.type || '').toLowerCase().includes(keywords)
				    )
				});
			}
		},
		methods: {
			editDatasource(type) {
				this.$router.push(`/datasource/edit/${type}`);
			}
		}
	});
})