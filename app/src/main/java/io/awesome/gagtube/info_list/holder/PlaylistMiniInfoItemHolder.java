package io.awesome.gagtube.info_list.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;

import io.awesome.gagtube.App;
import io.awesome.gagtube.R;
import io.awesome.gagtube.info_list.InfoItemBuilder;
import io.awesome.gagtube.util.GlideUtils;
import io.awesome.gagtube.util.Localization;

public class PlaylistMiniInfoItemHolder extends InfoItemHolder {
	
	public final ImageView itemThumbnailView;
	public final TextView itemStreamCountView;
	public final TextView itemTitleView;
	public final TextView itemUploaderView;
	public final ImageButton itemMoreActions;
	
	PlaylistMiniInfoItemHolder(InfoItemBuilder infoItemBuilder, int layoutId, ViewGroup parent) {
		
		super(infoItemBuilder, layoutId, parent);
		
		itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
		itemTitleView = itemView.findViewById(R.id.itemTitleView);
		itemStreamCountView = itemView.findViewById(R.id.itemStreamCountView);
		itemUploaderView = itemView.findViewById(R.id.itemUploaderView);
		itemMoreActions = itemView.findViewById(R.id.btn_action);
	}
	
	public PlaylistMiniInfoItemHolder(InfoItemBuilder infoItemBuilder, ViewGroup parent) {
		
		this(infoItemBuilder, R.layout.list_playlist_mini_item, parent);
	}
	
	@Override
	public void updateFromItem(final InfoItem infoItem) {
		
		if (!(infoItem instanceof PlaylistInfoItem)) return;
		final PlaylistInfoItem item = (PlaylistInfoItem) infoItem;
		
		itemTitleView.setText(item.getName());
		String streamCount;
		if (item.getStreamCount() <= 0) {
			streamCount = "∞ videos";
		} else {
			streamCount = Localization.localizeStreamCount(itemStreamCountView.getContext(), item.getStreamCount());
		}
		itemStreamCountView.setText(streamCount);
		itemUploaderView.setText(item.getUploaderName());
		GlideUtils.loadThumbnail(App.getAppContext(), itemThumbnailView, item.getThumbnailUrl());
		itemMoreActions.setVisibility(View.GONE);
		
		itemView.setOnClickListener(view -> {
			if (itemBuilder.getOnPlaylistSelectedListener() != null) {
				itemBuilder.getOnPlaylistSelectedListener().selected(item);
			}
		});
	}
}
