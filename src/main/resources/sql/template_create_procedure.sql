USE [nature]
GO

/****** Object:  StoredProcedure [dbo].[pc_remove_duplication]    Script Date: 05/24/2019 14:19:20 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

IF EXISTS(SELECT * FROM sys.procedures WHERE name = 'pc_remove_duplication')
  DROP procedure dbo.pc_remove_duplication
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
--		PRINT @artid

			SELECT aid INTO #tb_tmp_aut FROM nature.dbo.author WHERE artid = @artid


			SELECT affid INTO #tb_tmp_aff FROM nature.dbo.aff WHERE artid = @artid

			SELECT @aid = MIN(aid) FROM #tb_tmp_aut
			SELECT @affid = MIN(affid) FROM #tb_tmp_aff
			WHILE @aid IS NOT NULL AND @affid IS NOT NULL
				BEGIN TRY
					BEGIN TRAN
						DELETE FROM nature.dbo.author_aff
						WHERE aid = @aid
							AND affid = @affid
						DELETE FROM nature.dbo.author WHERE aid = @aid
						DELETE FROM nature.dbo.aff WHERE affid = @affid
						--DELETE FROM #tb_tmp_aut WHERE aid = @aid
						--DELETE FROM #tb_tmp_aff WHERE affid = @affid
						SELECT @aid = MIN(aid) FROM #tb_tmp_aut WHERE aid > @aid
						SELECT @affid = MIN(affid) FROM #tb_tmp_aff WHERE affid > @affid
					COMMIT TRAN
				END TRY
				BEGIN CATCH
					IF @@TRANCOUNT > 0
						BEGIN
							ROLLBACK TRAN
						END
				END CATCH
			--RETURN
			DELETE FROM nature.dbo.content WHERE artid = @artid
			--DELETE FROM #tb_tmp_art WHERE artid = @artid
			SELECT @artid = MIN(artid) FROM #tb_tmp_art WHERE artid > @artid
		END
	RETURN


  -- routine body goes here, e.g.
  -- SELECT 'Navicat for SQL Server'
END
GO