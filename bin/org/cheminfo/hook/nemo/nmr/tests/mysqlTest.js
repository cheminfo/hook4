var db = new DB.MySQL("localhost","test","","");
//console.log("connected");
// retrieve all available tables names
var names = db.getTableNames();
console.log(names);
var tableName = names[0].TABLE_NAME;
var structure = db.describeTable(tableName); // [{name:col1, type:INT...},{}]
//mmm, maybe the createTable function should be compatible with the describe table structure
db.dropTable("table2",{ifExists:true});
var def = [{name:"id",type:"INT(11)",notnull:true, primarykey:true, autoincrement:true},
           {name:"nucleus",type:"VARCHAR(128)"},
           {name:"frequency", type:"FLOAT(7,4)",notnull:false},
           {name:"date",type:"DATE"}];

db.createTable("table2", def);
//It is important to close the connection once it is used.

db.createIndex("table2", "frequency","theIndex");

var info = {field1:"EU",name:"Bell",age:10};
db.insert("table1", info);
db.delete2("table1", {key:"field1",value:"EU"});
var result = db.selectTable("table1", "name=\"andres\" ORDER BY age LIMIT 10", {format:"table"});
console.log(result);

var result2 = db.select("Select age FROM table1 WHERE name=\"andres\" ORDER BY age LIMIT 10", {format:"json"});
console.log(result2);
/*var result = db.update(tableName, structure);
var result = db.delete2(tableName, structure);*/
db.close();