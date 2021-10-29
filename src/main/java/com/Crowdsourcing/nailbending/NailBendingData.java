package com.Crowdsourcing.nailbending;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NailBendingData
{
	private String message;
	private boolean hasCrystalSaw;
	private int nailItemId;
	private int constructionLevel;
}
