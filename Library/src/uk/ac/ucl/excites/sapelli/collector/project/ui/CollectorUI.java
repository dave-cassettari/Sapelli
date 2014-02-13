package uk.ac.ucl.excites.sapelli.collector.project.ui;

import uk.ac.ucl.excites.sapelli.collector.project.model.*;

public interface CollectorUI
{

	public FieldUI createChoiceUI(ChoiceField cf);

	public FieldUI createPhotoUI(PhotoField pf);

	public FieldUI createAudioUI(AudioField af);

	public FieldUI createLocationUI(LocationField lf);

	public void setField(Field currentField);

}