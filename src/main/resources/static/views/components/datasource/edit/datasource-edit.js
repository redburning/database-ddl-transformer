define(function(require){
	var vue = require("vue");
	require('css!/views/components/datasource/edit/datasource-edit.css');
	return vue.extend({
		template: require('text!/views/components/datasource/edit/datasource-edit.html'),
		data () {
			return {
				icon: '',
				type: '',
				defaultJdbcUrl: '',
				datasource: {
					id: null,
					name: '',
					type: '',
					property: {
						url: '',
						database: '',
						user: '',
						passwd: ''
					},
					status: ''
				},
				testMsg: ''
			}
		},
		mounted () {
			const type = this.$route.params.type;
			this.type = type;
			this.datasource.type = type;
			this.icon = this.getDatasourceIcon(type);
			this.defaultJdbcUrl = this.getDefaultJdbcUrl(type);
			
			const id = this.$route.params.id;
			// 有id说明是edit, 无id说明是add
			if (id !== undefined) {
				this.getDatasource(id);
			}
		},
		methods: {
			deleteDatasource() {
				this.$confirm('删除 ' + this.datasource.type + ' 数据源, 是否继续?', '提示', {
		          	confirmButtonText: '确定',
		          	cancelButtonText: '取消',
		          	type: 'warning'
		        }).then(() => {
					this.$http.delete(`/datasource/${this.datasource.id}`)
						.then(response => {
							if (!!response.data && response.data.code === 200) {
								this.$router.push('/datasource/list');
							} else {
								this.$notify.error({
						          	title: '删除失败',
						          	message: response.data.msg || '未知错误'
						        });
							}
						})
						.catch(error => {
							this.$notify.error({
					          	title: '删除失败',
					          	message: error.message || '未知错误'
					        });
						});
		        });
			},
			testDatasource() {
				var self = this;
				this.$http.post('/datasource/test', self.datasource)
					.then(response => {
						if (!!response.data && response.data.code === 200) {
							self.datasource.status = 'success';
						} else {
							self.datasource.status = 'error';
							self.testMsg = response.data.msg;
						}
					})
					.catch(error => {
						self.datasource.status = 'error';
						self.testMsg = error.message;
					});
			},
			getDatasource(id) {
				var self = this;
				this.$http.get(`/datasource/${id}`)
					.then(response => {
						if (!!response.data && response.data.code === 200) {
							self.datasource = response.data.data;
						}
					})
					.catch(error => {
						console.log(error);
					});
			},
			saveDatasource() {
				var self = this;
				this.$http.post('/datasource', self.datasource)
					.then(response => {
						if (!!response.data && response.data.code === 200) {
							this.$router.push('/datasource/list');
						}
					})
					.catch(error => {
						this.$notify.error({
				          	title: '数据源保存失败',
				          	message: error.message || '未知错误'
				        });
					});
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
			getDefaultJdbcUrl(type) {
				if (type === 'MySQL') {
					return 'jdbc:mysql://127.0.0.1:3306';
				} else if (type === 'PostgreSQL') {
					return 'jdbc:postgresql://127.0.0.1:5432/postgres';
				} else if (type === 'Oracle') {
					return 'jdbc:oracle:thin:@//127.0.0.1:1521/orcl';
				} else {
					return '';
				}
			}
		}
	});
})