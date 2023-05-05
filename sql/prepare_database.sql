DROP TABLE IF EXISTS usercards;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS users;

CREATE TABLE cards(
	id int auto_increment PRIMARY KEY,
	word varchar(255) NOT NULL,
	option1correct varchar(255) NOT NULL,
	option2incorrect varchar(255) NOT NULL,
	option3incorrect varchar(255) NOT NULL
);

INSERT INTO cards (word, option1correct, option2incorrect, option3incorrect) VALUES
	('apple', 'яблоко', 'якорь', 'крыша'),
	('car', 'машина', 'телефон', 'круг'),
	('ball', 'мяч', 'друг', 'ворота'),
	('orange', 'оранжевый', 'жёлтый', 'полосатый'),
	('sun', 'солнце', 'звезда', 'имя'),
	('moon', 'луна', 'созвездие', 'корова'),
	('length', 'длина', 'ширина', 'вес'),
	('read', 'читать', 'говорить', 'слышать'),
	('day', 'день', 'вечер', 'утро'),
	('night', 'ночь', 'тьма', 'луна'),
	('cow', 'корова', 'сорока', 'слон'),
	('chicken', 'курица', 'победа', 'обед'),
	('carrot', 'морковь', 'свекла', 'картошка'),
	('rabbit', 'кролик', 'волк', 'суслик'),
	('helicopter', 'вертолет', 'самолет', 'конвертоплан');

CREATE TABLE users(
	id int auto_increment PRIMARY KEY,
	username varchar(255) NOT NULL
);

CREATE TABLE usercards(
	user_id int NOT NULL,
	card_id int NOT NULL,
	box int NOT NULL DEFAULT 1,
	PRIMARY KEY(user_id, card_id),
	FOREIGN KEY(user_id) REFERENCES users(id),
	FOREIGN KEY(card_id) REFERENCES cards(id)
);
