define(function(require){
	var vue = require("vue");
	require('css!/views/components/task/edit/task-edit.css');
	return vue.extend({
		template: require('text!/views/components/task/edit/task-edit.html'),
		components: {
			datasourceSelect: require('/views/components/datasource/select/datasource-select.js')
		},
		data() {
			return {
				datasourceOptions: [],
	            sourceDatasource: {},
	            targetDatasource: {},
	            sourceDatabaseOptions: [],
	            targetDatabaseOptions: [],
	            sourceTableList: [],
	            targetTableList: [],
	            task: {
					name: '',
					sourceDatasourceId: '',
					targetDatasourceId: '',
					sourceDatabase: '',
					targetDatabase: '',
					subtasks: []
				},
	            filteredTableList: [],
	            searchInput: '',
	            createModeOptions: [
					{ label: '新建表', value: 'create' },
					{ label: '删除已有表并新建', value: 'dropExistAndCreate' },
					{ label: '暂不新建', value: 'notCreate' },
					{ label: '使用已有表', value: 'useExist' }
				],
				loading: false,
				editSqlDialogVisible: false,
				saveTaskDialogVisible: false,
				currentSQL: '',
				progress: 0,
				progressMsg: '',
				taskRunning: false,
				codeEditor: null,
				selectedRows: [],
				editRowIndex: null,				// 编辑sql的行编号
				transformFinished: false,		// ddl转换完成
			}
		},
		computed: {
			countMatchedTables() {
				return this.task.subtasks.filter(task => task.targetTable != null).length;
			}
		},
		created() {
			this.getDatasourceOptions();
		},
		mounted() {
			const taskId = this.$route.params.id;
			if (taskId !== null && taskId !== undefined) {
				this.getTask(taskId);
			}
		},
		watch: {},
		methods: {
			async onSourceDatasourceChange() {
				this.sourceDatabaseOptions = await this.getDatabaseOptions(this.sourceDatasource.key);
				this.task.sourceDatasourceId = this.sourceDatasource.key;
			},
			async onTargetDatasourceChange() {
				this.targetDatabaseOptions = await this.getDatabaseOptions(this.targetDatasource.key);
				this.task.targetDatasourceId = this.targetDatasource.key;
			},
			async onSourceDatabaseChange() {
				this.loading = true;
				this.sourceTableList = await this.getTableList(this.sourceDatasource.key, this.task.sourceDatabase);
				this.matchTable();
				this.loading = false;
			},
			async onTargetDatabaseChange() {
				this.loading = true;
				this.targetTableList = await this.getTableList(this.targetDatasource.key, this.task.targetDatabase);
				this.matchTable();
				this.loading = false;
			},
			getDatasourceOptions() {
				var self = this;
				var datasourceList = [];
				this.$http.get('/datasource/list')
					.then(response => {
						if (!!response.data) {
							var responseData = response.data;
							datasourceList = responseData.data;
							self.datasourceOptions = datasourceList.map(datasource => {
								var icon = self.getDatasourceIcon(datasource.type);
								return {
									key: datasource.id,
									label: datasource.name,
									type: datasource.type,
									icon: icon
								};
							});
						}
					})
					.catch(error => {
						console.log(error);
					});
			},
			async getDatabaseOptions(id) {
				try {
					const response = await this.$http.get(`/datasource/${id}/databases`);
					if (!!response.data) {
						var responseData = response.data;
						var databaseOptions = responseData.data;
						return databaseOptions.map(item => ({ value: item, label: item }));
					}
					return [];
				} catch (e) {
					console.log(e);
					return [];
				}
			},
			async getTableList(id, database) {
				try {
					const response = await this.$http.get(`/datasource/${id}/${database}/tables`);
					if (!!response.data) {
						var responseData = response.data;
						return responseData.data;
					}
					return [];
				} catch (e) {
					console.log(e);
					return[];
				}
			},
			matchTable() {
				this.task.subtasks = [];
				// 创建一个目标表的映射，键为小写表名，值为目标表名
				const targetTableMap = this.targetTableList.reduce((map, table) => {
				    map[table.toLowerCase()] = table;
				    return map;
				}, {});
				
				// 遍历源表列表，进行匹配
				this.sourceTableList.forEach(sourceTable => {
				    const targetTable = targetTableMap[sourceTable.toLowerCase()];
				    this.task.subtasks.push({
						sourceDatabase: this.task.sourceDatabase,
						targetDatabase: this.task.targetDatabase,
				        sourceTable: sourceTable,
				        targetTable: targetTable || null,
				        createMode: targetTable == null ? 'create' : 'notCreate',
				        sql: null,
				        transformStatus: null,
				        executeStatus: null,
				        errorMsg: null
				    });
				});
				this.filteredTableList = this.task.subtasks;
			},
			filterTable() {
				if (!this.searchInput) {
					this.filteredTableList = this.task.subtasks;
					return;
				}
				this.filteredTableList = this.task.subtasks.filter((item) => {
					return ((item.sourceDatabase ? item.sourceDatabase.toLowerCase().includes(this.searchInput.toLowerCase()) : false) ||
							(item.sourceTable ? item.sourceTable.toLowerCase().includes(this.searchInput.toLowerCase()) : false) ||
							(item.targetDatabase ? item.targetDatabase.toLowerCase().includes(this.searchInput.toLowerCase()) : false) ||
							(item.targetTable ? item.targetTable.toLowerCase().includes(this.searchInput.toLowerCase()) : false));
				});
			},
			showAll() {
				this.filteredTableList = this.task.subtasks;
			},
			filterTransformSuccess() {
				this.filteredTableList = this.task.subtasks.filter((item) => {
					return ((item.transformStatus ? item.transformStatus.includes('成功') : false));
				});
			},
			filterTransformFailed() {
				this.filteredTableList = this.task.subtasks.filter((item) => {
					return ((item.transformStatus ? item.transformStatus.includes('失败') : false));
				});
			},
			filterExecuteSuccess() {
				this.filteredTableList = this.task.subtasks.filter((item) => {
					return ((item.executeStatus ? item.executeStatus.includes('成功') : false));
				});
			},
			filterExecuteFailed() {
				this.filteredTableList = this.task.subtasks.filter((item) => {
					return ((item.executeStatus ? item.executeStatus.includes('失败') : false));
				});
			},
			filterConflict() {
				this.filteredTableList = this.task.subtasks.filter((item) => {
					return ((item.targetTable ? item.targetTable !== null && item.targetTable.trim() !== '' : false));
				});
			},
			handleSelectionChange(selection) {
				this.selectedRows = selection;
			},
			deleteSelectedRows() {
				this.$confirm('此操作将删除 ' + this.selectedRows.length + ' 行数据, 是否继续?', '提示', {
		          	confirmButtonText: '确定',
		          	cancelButtonText: '取消',
		          	type: 'warning'
		        }).then(() => {
					this.selectedRows.forEach(row => {
						var index = this.filteredTableList.indexOf(row);
						if (index > -1) {
							this.filteredTableList.splice(index, 1);
						}
						index = this.task.subtasks.indexOf(row);
						if (index > -1) {
							this.task.subtasks.splice(index, 1);
						}
					});
					this.selectedRows = [];
			        this.$message({
			            type: 'success',
			            message: '删除成功!'
			        });
		        });
			},
			startTransform() {
				var self = this;
				self.progress = 0;
				self.progressMsg = '';
				self.taskRunning = true;
				this.$http.post('/task/ddltransfer', self.task)
					.then(response => {
						if (!!response.data && response.data.code === 200) {
							var taskId = response.data.data;
							
							// 创建 EventSource 对象，连接到服务器端点
					        const eventSource = new EventSource(`/task/ddltransfer-progress/${taskId}`);
					        
					        // 监听进度更新事件
					        eventSource.addEventListener('progress', function(event) {
					            var json =  JSON.parse(event.data);
					            self.progress = json.progress;
					            self.progressMsg = json.message;
					            var current = parseInt(json.message.split('/')[0]);
					            var data = json.data;
					            var sql = Object.values(data)[0];
					            self.task.subtasks[current - 1].transformStatus = '转换成功';
					            self.task.subtasks[current - 1].sql = sql;
					        });
					        
					        // 监听最终结果事件
					        eventSource.addEventListener('result', function(event) {
								self.progress = 1;
					            var json =  JSON.parse(event.data);
					            self.task.subtasks.forEach(subtask => {
									const sql = json[subtask.sourceDatabase + "." + subtask.sourceTable];
									if (sql !== null && sql !== undefined) {
										subtask['sql'] = sql;
									}
								});
					            // 关闭 EventSource 连接
					            eventSource.close();
					            self.taskRunning = false;
					            self.transformFinished = true;
					        });
					        
					        // 错误处理
					        eventSource.onerror = function(event) {
					            console.error('SSE error:', event);
					            eventSource.close();
					            self.taskRunning = false;
					        };
						} else {
							// 任务创建出错，弹出提示信息
						}
					})
					.catch(error => {
						console.log(error);
					});
			},
			executeCreateTable() {
				var self = this;
				self.progress = 0;
				self.progressMsg = '';
				self.taskRunning = true;
				this.$http.post('/task/ddlexecute-async', self.task)
					.then(response => {
						if (!!response.data && response.data.code === 200) {
							var taskId = response.data.data;
							
							// 创建 EventSource 对象，连接到服务器端点
					        const eventSource = new EventSource(`/task/ddlexecute-async-progress/${taskId}`);
					        
					        // 监听进度更新事件
					        eventSource.addEventListener('progress', function(event) {
					            var json =  JSON.parse(event.data);
					            self.progress = json.progress;
					            self.progressMsg = json.message;
					            var current = parseInt(json.message.split('/')[0]);
					            self.task.subtasks[current - 1].executeStatus = '执行成功';
					        });
					        
					        // 监听执行失败事件
					        eventSource.addEventListener('error', function(event) {
					            var json =  JSON.parse(event.data);
					            var index = json.index;
					            self.task.subtasks[index - 1].executeStatus = '创建失败';
					            self.task.subtasks[index - 1].errorMsg = json.message;
					        });
					        
					        // 监听最终结果事件
					        eventSource.addEventListener('result', function(event) {
								self.progress = 1;
					            // 关闭 EventSource 连接
					            eventSource.close();
					            self.taskRunning = false;
					        });
					        
					        // 错误处理
					        eventSource.onerror = function(event) {
					            console.error('SSE error:', event);
					            eventSource.close();
					            self.taskRunning = false;
					        };
						} else {
							// 任务创建出错，弹出提示信息
						}
					})
					.catch(error => {
						console.log(error);
					});
			},
			viewSQL(rowIndex) {
				this.editRowIndex = rowIndex;
				var sql = this.task.subtasks[rowIndex].sql;
				this.currentSQL = sql;
				this.editSqlDialogVisible = true;
				this.$nextTick(() => {
					this.codeEditor = CodeMirror.fromTextArea(document.getElementById("code"), {
			            mode: "text/x-sql",
			            theme: "panda-syntax",
			            lineNumbers: false
			        });
			        this.codeEditor.setValue(this.currentSQL);
				});
			},
			executeSQL(rowIndex) {
				var self = this;
				this.$http.post('/task/ddlexecute', {
					targetDatasourceId: this.targetDatasource.key,
					subtasks: [this.task.subtasks[rowIndex]]
				}).then(response => {
					if (!!response.data && response.data.code === 200) {
						self.task.subtasks[rowIndex].executeStatus = '执行成功';
						this.$notify({
				          	title: '执行成功',
				          	type: 'success'
				        });
					} else {
						self.task.subtasks[rowIndex].executeStatus = '创建失败';
						this.$notify.error({
				          	title: '创建失败',
				          	message: response.data.msg || '未知错误'
				        });
					}
				}).catch(error => {
					self.task.subtasks[rowIndex].executeStatus = '创建失败';
					this.$notify.error({
			          	title: '创建失败',
			          	message: error.message || '未知错误'
			        });
				});
			},
			saveSQL() {
				this.task.subtasks[this.editRowIndex].sql = this.codeEditor.getValue();
				this.editSqlDialogVisible = false;
			},
			saveTask() {
				var self = this;
				this.$http.put('/task/save', self.task)
					.then(response => {
						if (!!response.data && response.data.code === 200) {
							this.$router.push('/task/list');
						} else {
							this.$notify.error({
					          	title: '保存失败',
					          	message: response.data.msg || '未知错误'
					        });
						}
						self.saveTaskDialogVisible = false;
					})
					.catch(error => {
						this.$notify.error({
				          	title: '保存失败',
				          	message: error.message || '未知错误'
				        });
					});
			},
			async getTask(id) {
				var self = this;
				try {
					const response = await this.$http.get(`/task/${id}`);
					if (!!response.data && response.data.code === 200) {
						var task = response.data.data;
						self.sourceDatasource = {
							key: task.sourceDatasource.id,
							label: task.sourceDatasource.name,
							type: task.sourceDatasource.type,
							icon: self.getDatasourceIcon(task.sourceDatasource.type)
						};
						self.targetDatasource = {
							key: task.targetDatasource.id,
							label: task.targetDatasource.name,
							type: task.targetDatasource.type,
							icon: self.getDatasourceIcon(task.targetDatasource.type)
						};
						self.task = task;
						self.filteredTableList = self.task.subtasks;
						
						self.sourceDatabaseOptions = await self.getDatabaseOptions(self.sourceDatasource.key);
						self.targetDatabaseOptions = await self.getDatabaseOptions(self.targetDatasource.key);
					}
				} catch (error) {
					console.log(error);
					// 任务获取出错，弹出提示信息
				}
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
			getTransformStatusClass(status) {
				if (status.indexOf('成功') !== -1 || status.indexOf('success') !== -1) {
					return 'highlight-green';
				} else {
					return 'highlight-red';
				}
			},
			getExecuteStatusClass(status) {
				if (status.indexOf('成功') !== -1 || status.indexOf('success') !== -1) {
					return 'highlight-blue';
				} else {
					return 'highlight-red';
				}
			}
		}
	});
})