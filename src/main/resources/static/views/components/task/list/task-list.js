define(function(require){
	var vue = require("vue");
	require('css!/views/components/task/list/task-list.css');
	return vue.extend({
		template: require('text!/views/components/task/list/task-list.html'),
		data () {
			return {
				searchInput: '',
				taskList: []
			}
		},
		computed: {
			filteredTaskList() {
				const keywords = this.searchInput;
				return this.taskList.filter(item => {
				    return (
				        (item.name || '').toLowerCase().includes(keywords) ||
				        (item.sourceDatasourceType || '').toLowerCase().includes(keywords) ||
				        (item.targetDatasourceType || '').toLowerCase().includes(keywords)
				    )
				});
			}
		},
		mounted () {
			this.listTask();
		},
		methods: {
			listTask() {
				var self = this;
				self.taskList = [];
				this.$http.get('/task/list')
					.then(response => {
						if (!!response.data) {
							var responseData = response.data;
							var taskList = responseData.data;
							taskList.forEach(task => {
								var sourceIcon = self.getDatasourceIcon(task.sourceDatasource.type);
								task.sourceDatasource.icon = sourceIcon;
								var targetIcon = self.getDatasourceIcon(task.targetDatasource.type);
								task.targetDatasource.icon = targetIcon;
							});
							self.taskList = taskList;
						}
					})
					.catch(error => {
						console.log(error);
					});
			},
			addTask() {
				this.$router.push('/task/edit');
			},
			editTask(id) {
				this.$router.push(`/task/edit/${id}`);
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
			}
		}
	});
})