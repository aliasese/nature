USE [nature]
GO

/****** Object:  StoredProcedure [dbo].[pc_remove_duplication]    Script Date: 05/27/2019 14:37:48 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

IF EXISTS(SELECT * FROM sys.procedures WHERE name = 'pc_remove_duplication')
    BEGIN
      PRINT 'PROCEDURE: pc_remove_duplication has been existed already'
      DROP procedure dbo.pc_remove_duplication
    END
GO

CREATE PROCEDURE [dbo].[pc_remove_duplication]
( @jabt varchar(50) , @issn8 char(8), @volume char(12),
	@issue char(10), @atl varchar(920),
  @ppf char(5), @ppl char(5)
)
AS
BEGIN
	DECLARE @tb_tmp_art TABLE(artid INT)
	DECLARE	@artid INT
	DECLARE @tb_tmp_aut TABLE(aid INT)
	DECLARE @aid INT
	DECLARE @tb_tmp_aff TABLE(affid INT)
	DECLARE @affid INT
	SELECT artid INTO #tb_tmp_art
	FROM nature.dbo.content
	WHERE jabt = @jabt AND issn8 = @issn8 AND vid = @volume AND iid = @issue
		AND atl = @atl AND ppf = @ppf AND ppl = @ppl
	SELECT @artid = MIN(artid) FROM #tb_tmp_art
	WHILE @artid IS NOT NULL
		BEGIN
			SELECT aid INTO #tb_tmp_aut FROM nature.dbo.author WHERE artid = @artid
			SELECT affid INTO #tb_tmp_aff FROM nature.dbo.aff WHERE artid = @artid
			SELECT @aid = MIN(aid) FROM #tb_tmp_aut
			BEGIN TRY
				WHILE @aid IS NOT NULL
					BEGIN
						SELECT @affid = MIN(affid) FROM #tb_tmp_aff
						WHILE @affid IS NOT NULL
							BEGIN
								BEGIN TRAN T1
									DELETE FROM nature.dbo.author_aff
										WHERE aid = @aid
											AND affid = @affid
								COMMIT TRAN T1
								SELECT @affid = MIN(affid) FROM #tb_tmp_aff WHERE affid > @affid
							END
						SELECT @aid = MIN(aid) FROM #tb_tmp_aut WHERE aid > @aid
					END
				BEGIN TRAN T2
					DELETE FROM nature.dbo.aff WHERE artid = @artid
					DELETE FROM nature.dbo.author WHERE artid = @artid
					DELETE FROM nature.dbo.content WHERE artid = @artid
				COMMIT TRAN T2
			END TRY
			BEGIN CATCH
				IF @@TRANCOUNT > 0
					BEGIN
						ROLLBACK TRAN
					END
				ELSE
					BEGIN
						PRINT 'SUCCESS TO CASCADING DELETE'
					END
			END CATCH
			SELECT @artid = MIN(artid) FROM #tb_tmp_art WHERE artid > @artid
		END
	RETURN
END
GO