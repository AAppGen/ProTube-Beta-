package io.awesome.gagtube.info_list.holder;

import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.Description;

import java.util.function.Consumer;

import io.awesome.gagtube.R;
import io.awesome.gagtube.info_list.InfoItemBuilder;
import io.awesome.gagtube.util.GlideUtils;
import io.awesome.gagtube.util.Localization;
import io.awesome.gagtube.util.NavigationHelper;
import io.awesome.gagtube.util.SharedUtils;
import io.awesome.gagtube.util.text.CommentTextOnTouchListener;
import io.awesome.gagtube.util.text.TextLinkifier;
import io.reactivex.disposables.CompositeDisposable;

public class CommentsInfoItemHolder extends InfoItemHolder {
    private static final String ELLIPSIS = "…";

    private static final int COMMENT_DEFAULT_LINES = 1000;
    private static final int COMMENT_EXPANDED_LINES = 1000;

    private final Paint paintAtContentSize;
    private final float ellipsisWidthPx;

    public final TextView itemTitleView;
    private final ImageView itemHeartView;
    private final ImageView itemPinnedView;
    private final ImageView itemThumbnailView;
    private final TextView itemContentView;
    private final TextView itemLikesCountView;

    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    private Description commentText;
    @Nullable
    private StreamingService streamService;
    @Nullable
    private String streamUrl;

    CommentsInfoItemHolder(final InfoItemBuilder infoItemBuilder, final int layoutId, final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);

        itemTitleView = itemView.findViewById(R.id.itemTitleView);
        itemHeartView = itemView.findViewById(R.id.detail_heart_image_view);
        itemPinnedView = itemView.findViewById(R.id.detail_pinned_view);
        itemThumbnailView = itemView.findViewById(R.id.itemThumbnailView);
        itemLikesCountView = itemView.findViewById(R.id.detail_thumbs_up_count_view);
        itemContentView = itemView.findViewById(R.id.itemCommentContentView);

        paintAtContentSize = new Paint();
        paintAtContentSize.setTextSize(itemContentView.getTextSize());
        ellipsisWidthPx = paintAtContentSize.measureText(ELLIPSIS);
    }

    public CommentsInfoItemHolder(final InfoItemBuilder infoItemBuilder, final ViewGroup parent) {
        this(infoItemBuilder, R.layout.list_comments_item, parent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void updateFromItem(final InfoItem infoItem) {
        if (!(infoItem instanceof CommentsInfoItem)) {
            return;
        }
        final CommentsInfoItem item = (CommentsInfoItem) infoItem;

        String publishedTime;
        if (item.getUploadDate() != null) {
            publishedTime = Localization.relativeTime(item.getUploadDate().date());
        } else {
            publishedTime = item.getTextualUploadDate();
        }
        itemTitleView.setText(Localization.concatenateStrings(item.getUploaderName(), publishedTime));

        itemHeartView.setVisibility(item.isHeartedByUploader() ? View.VISIBLE : View.GONE);

        itemPinnedView.setVisibility(item.isPinned() ? View.VISIBLE : View.GONE);

        GlideUtils.loadAvatar(itemBuilder.getContext(), itemThumbnailView, item.getUploaderAvatarUrl());

        itemThumbnailView.setOnClickListener(view -> openCommentAuthor(item));

        try {
            streamService = NewPipe.getService(item.getServiceId());
        } catch (final ExtractionException e) {
            streamService = ServiceList.YouTube;
        }
        streamUrl = item.getUrl();
        commentText = item.getCommentText();
        ellipsize();

        itemContentView.setOnTouchListener(CommentTextOnTouchListener.INSTANCE);

        if (item.getLikeCount() >= 0) {
            itemLikesCountView.setText(Localization.shortCount(itemBuilder.getContext(), item.getLikeCount()));
        } else {
            itemLikesCountView.setText("N/A");
        }

        itemView.setOnClickListener(view -> {
            toggleEllipsize();
            if (itemBuilder.getOnCommentsSelectedListener() != null) {
                itemBuilder.getOnCommentsSelectedListener().selected(item);
            }
        });

        itemView.setOnLongClickListener(view -> {
            final CharSequence text = itemContentView.getText();
            if (text != null) {
                SharedUtils.copyToClipboard(itemBuilder.getContext(), text.toString());
            }
            return true;
        });
    }

    private void openCommentAuthor(final CommentsInfoItem item) {
        if (isEmpty(item.getUploaderUrl())) {
            return;
        }
        final AppCompatActivity activity = (AppCompatActivity) itemBuilder.getContext();
        try {
            NavigationHelper.openChannelFragment(activity.getSupportFragmentManager(), item.getServiceId(), item.getUploaderUrl(), item.getUploaderName());
        } catch (Exception ignored) {
        }
    }

    private void allowLinkFocus() {
        itemContentView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void denyLinkFocus() {
        itemContentView.setMovementMethod(null);
    }

    private boolean shouldFocusLinks() {
        if (itemView.isInTouchMode()) {
            return false;
        }

        final URLSpan[] urls = itemContentView.getUrls();

        return urls != null && urls.length != 0;
    }

    private void determineMovementMethod() {
        if (shouldFocusLinks()) {
            allowLinkFocus();
        } else {
            denyLinkFocus();
        }
    }

    private void ellipsize() {
        itemContentView.setMaxLines(COMMENT_EXPANDED_LINES);
        linkifyCommentContentView(v -> {
            boolean hasEllipsis = false;

            final CharSequence charSeqText = itemContentView.getText();
            if (charSeqText != null && itemContentView.getLineCount() > COMMENT_DEFAULT_LINES) {
                // Note that converting to String removes spans (i.e. links), but that's something
                // we actually want since when the text is ellipsized we want all clicks on the
                // comment to expand the comment, not to open links.
                final String text = charSeqText.toString();

                final Layout layout = itemContentView.getLayout();
                final float lineWidth = layout.getLineWidth(COMMENT_DEFAULT_LINES - 1);
                final float layoutWidth = layout.getWidth();
                final int lineStart = layout.getLineStart(COMMENT_DEFAULT_LINES - 1);
                final int lineEnd = layout.getLineEnd(COMMENT_DEFAULT_LINES - 1);

                // remove characters up until there is enough space for the ellipsis
                // (also summing 2 more pixels, just to be sure to avoid float rounding errors)
                int end = lineEnd;
                float removedCharactersWidth = 0.0f;
                while (lineWidth - removedCharactersWidth + ellipsisWidthPx + 2.0f > layoutWidth
                        && end >= lineStart) {
                    end -= 1;
                    // recalculate each time to account for ligatures or other similar things
                    removedCharactersWidth = paintAtContentSize.measureText(
                            text.substring(end, lineEnd));
                }

                // remove trailing spaces and newlines
                while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) {
                    end -= 1;
                }

                final String newVal = text.substring(0, end) + ELLIPSIS;
                itemContentView.setText(newVal);
                hasEllipsis = true;
            }

            itemContentView.setMaxLines(COMMENT_DEFAULT_LINES);
            if (hasEllipsis) {
                denyLinkFocus();
            } else {
                determineMovementMethod();
            }
        });
    }

    private void toggleEllipsize() {
        final CharSequence text = itemContentView.getText();
        if (!isEmpty(text) && text.charAt(text.length() - 1) == ELLIPSIS.charAt(0)) {
            expand();
        } else if (itemContentView.getLineCount() > COMMENT_DEFAULT_LINES) {
            ellipsize();
        }
    }

    private void expand() {
        itemContentView.setMaxLines(COMMENT_EXPANDED_LINES);
        linkifyCommentContentView(v -> determineMovementMethod());
    }

    private void linkifyCommentContentView(@Nullable final Consumer<TextView> onCompletion) {
        disposables.clear();
        if (commentText != null) {
            TextLinkifier.fromDescription(itemContentView, commentText, HtmlCompat.FROM_HTML_MODE_LEGACY, streamService, streamUrl, disposables, onCompletion);
        }
    }
}
