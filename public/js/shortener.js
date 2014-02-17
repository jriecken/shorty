var Shortener = (function () {
  var shortenFormGroup, shortenField, shortenButton, validationError, resultsArea, shortenedUrlLink, shortenedUrlStatsLink;

  /**
   * Show (or clear) a validation error
   */
  function setValidationError(error) {
    if (!error) {
      shortenFormGroup.removeClass('has-error');
      validationError.hide();
    } else {
      shortenFormGroup.addClass('has-error');
      validationError.text(error).show();
    }
  }

  /**
   * Shorten the URL if enter is pressed.
   */
  function keyShortenUrl(ev) {
    if (ev.which === 13) {
      shortenUrl();
    }
  }

  /**
   * Shorten a URL, showing the results area if successful or a validation error if not.
   */
  function shortenUrl() {
    resultsArea.hide();
    var url = $.trim(shortenField.val());
    $.ajax({
      url: '/v1/urls',
      type: 'POST',
      data: JSON.stringify({
        long_url: url
      }),
      contentType: 'application/json',
      statusCode: {
        200: function (data) {
          setValidationError();
          var shortUrl = data.short_url;
          var statsUrl = shortUrl + '/stats';
          shortenedUrlLink.attr('href', shortUrl).text(shortUrl);
          shortenedUrlStatsLink.attr('href', statsUrl).text(statsUrl);
          shortenField.val('');
          resultsArea.show();
        },
        400: function () {
          setValidationError('Enter a valid http/https URL that is less than 2048 characters long');
        },
        500: function () {
          setValidationError('Oops! Something unexpected happened. Please try again!');
        }
      }
    });
  }

  /**
   * Initialize the URL shortening page.
   */
  function init() {
    shortenFormGroup = $('#shortenFormGroup');
    shortenField = $('#shortenField');
    shortenButton = $('#shortenButton');
    validationError = $('#validationError');
    resultsArea = $('#resultsArea');
    shortenedUrlLink = $('#shortenedUrlLink');
    shortenedUrlStatsLink = $('#shortenedUrlStatsLink');
    shortenButton.on('click', shortenUrl);
    shortenField.on('keydown', keyShortenUrl);
    shortenField.focus();
  }

  return {
    init: init
  };
}());
