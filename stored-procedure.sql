DROP PROCEDURE IF EXISTS add_movie;

DELIMITER //
CREATE PROCEDURE add_movie(
	IN star_name CHAR(100),
    IN genre_name CHAR(32),
    IN movie_title CHAR(100),
    IN movie_year DECIMAL,
    IN movie_director CHAR(100)
)

BEGIN
    DECLARE new_movie_id VARCHAR(10);
    DECLARE new_star_id VARCHAR(10);
	DECLARE genre_exists BOOL;
    DECLARE star_exists BOOL;
    DECLARE movie_exists BOOL;
    
	SET movie_exists = EXISTS(SELECT * from movies WHERE title = movie_title AND `year` = movie_year AND director = movie_director);
    
    IF (NOT movie_exists) THEN
		SET new_movie_id = CONCAT('tt', LPAD(CAST((CAST(SUBSTRING((SELECT MAX(id) from movies), 3, 7) as DECIMAL) + 1) AS CHAR(10)), 7, '0'));
		SET new_star_id = CONCAT('nm', LPAD(CAST((CAST(SUBSTRING((SELECT MAX(id) from stars), 3, 7) as DECIMAL) + 1) AS CHAR(10)), 7, '0'));
		SET star_exists = EXISTS(SELECT * from stars where `name` = star_name);	
        IF (NOT star_exists) THEN
            INSERT INTO stars (id, `name`) VALUES(new_star_id, star_name);
		END IF;
        SET genre_exists = EXISTS(SELECT * from genres WHERE `name` = genre_name);
        IF (NOT genre_exists) THEN 
			INSERT INTO genres (`name`) VALUES(genre_name);
		END IF;
		INSERT INTO movies VALUES(new_movie_id, movie_title, movie_year, movie_director);
	INSERT INTO ratings VALUES(new_movie_id, 0.0, 0);
        INSERT INTO genres_in_movies VALUES((SELECT id from genres WHERE `name` = genre_name), new_movie_id);
        INSERT INTO stars_in_movies VALUES((SELECT id from stars where `name` = star_name LIMIT 1), new_movie_id);
	END IF;
	SELECT movie_exists;
END //
DELIMITER ;