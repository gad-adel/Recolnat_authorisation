CREATE OR REPLACE FUNCTION change_modified_at()
  RETURNS TRIGGER 
  LANGUAGE PLPGSQL
  AS
$$
BEGIN
	NEW.data_change_ts := NOW();
	RETURN NEW;
END;
$$ 
;
  
CREATE TRIGGER institution_modified_at
  BEFORE INSERT OR UPDATE
  ON institution
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();
  
CREATE TRIGGER collection_modified_at
  BEFORE INSERT OR UPDATE
  ON collection
  FOR EACH ROW
  EXECUTE PROCEDURE change_modified_at();
  
-- delimiter ;
-- rollback DROP TRIGGER IF EXISTS taxon_modified_at;