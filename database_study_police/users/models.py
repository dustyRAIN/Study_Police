from django.db import models
from django.contrib.auth.models import AbstractUser
from django.utils.translation import ugettext_lazy as _

from .managers import CustomUserManager

class CustomUser(AbstractUser):
    username = None
    email = models.EmailField(_('email address'), unique=True)
    gender = models.IntegerField(null = True)
    name = models.CharField(max_length = 100, null = True)

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = ['gender']


    objects = CustomUserManager()

    image = models.ImageField(upload_to = 'profile_pictures/')
    
    
    def __str__(self):
        return self.email

    def getID(self):
        return str(self.id)



class DemoPhoto(models.Model):
    name = models.CharField(max_length = 100)
    size = models.IntegerField()
    image = models.ImageField(upload_to = 'demo_pictures/')

    def __str__(self):
        return self.name


class EmailByProvider(models.Model):
    id = models.AutoField(primary_key = True)
    email = models.ForeignKey(CustomUser, on_delete=models.CASCADE)
    provider = models.IntegerField()

    def __str__(self):
        return str(self.provider) + " " + str(self.email)
