import base64
from apiclient import errors
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from httplib2 import Http
from email.mime.text import MIMEText

# Email variables. Modify this!
#EMAIL_FROM = 'villaget3ch@gmail.com'

EMAIL_FROM = 'admin-420@services-1729.iam.gserviceaccount.com'
#EMAIL_FROM = 'servitor@services-1729.iam.gserviceaccount.com'

EMAIL_TO = 'anubhav.balodhi@gmail.com'
EMAIL_SUBJECT = 'Hello, from Zippe!'
EMAIL_CONTENT = 'Hi, this is a test\nZippe\nhttps://zippe.in'

SERVICE_ACCOUNT_FILE = '/home/Anubhav Pandey/work/zp_exp/server/google-cloud/services-1729-d85e5765d66c.json'


def service_account_login():
	credentials = Credentials.from_service_account_file(
		SERVICE_ACCOUNT_FILE, 
		scopes=['https://www.googleapis.com/auth/gmail.send']
	)
	delegated_credentials = credentials.with_subject('me')
	gmail = build('gmail', 'v1', credentials=delegated_credentials)
	return gmail

def create(from_, to_, subject, text):
	"""Create a message for an email

	Parameters
	----------
	from_ : [type]
		Email address of the sender
	to_ : [type]
		Email address of the receiver
	subject : [type]
		 The subject of the email message
	text : [type]
		The text of the email message

	Returns
	-------
	[type]
		An object containing a base64url encoded email object
	"""

	message = MIMEText(text)
	message['from'] = from_
	message['to'] = to_
	message['subject'] = subject

	return {
		'raw': base64.urlsafe_b64encode(message.as_string().encode()).decode()
	}

def send(from_, to_, subject, text):
	"""[summary]

	Parameters
	----------
	service : [type]
		Authorized Gmail API service instance
	message : [type]
		Message to be sent

	Returns
	-------
	[type]
		Sent Message
	"""

	try:
		message_ = create(from_=from_, to_ = to_, subject=subject, text=text)
		service = service_account_login()
		message = (
			service.users()
			.messages()
			.send(userId='', body=message_)
			.execute()
		)
		print(f"Message Id: {message['id']}")

		return message

	except errors.HttpError as error:
		print(f'An error occurred: {error}')
		


def send_message(service, user_id, message):
  """Send an email message.

  Args:
    service: Authorized Gmail API service instance.
    user_id: User's email address. The special value "me"
    can be used to indicate the authenticated user.
    message: Message to be sent.

  Returns:
    Sent Message.
  """
  try:
    message = (service.users().messages().send(userId=user_id, body=message)
               .execute())
    print('Message Id: %s' % message['id'])
    return message
  except errors.HttpError as error:
    print('An error occurred: %s' % error)

service = service_account_login()
# Call the Gmail API
message = create(EMAIL_FROM, EMAIL_TO, EMAIL_SUBJECT, EMAIL_CONTENT)
sent = send_message(service,'me', message)