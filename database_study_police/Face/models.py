from django.db import models
from users.models import CustomUser
import os


class Face(models.Model):
    id = models.AutoField(primary_key = True)
    image = models.ImageField(upload_to = 'Faces/')
    user = models.ForeignKey(CustomUser, on_delete=models.CASCADE)

    def __str__(self):
        return str(self.user)

    def delete(self, using=None, keep_parents=False):
        self.image.delete()
        return super().delete(using=using, keep_parents=keep_parents)


class TestFace(models.Model):
    id = models.AutoField(primary_key = True)
    test_image = models.ImageField(upload_to = 'TestFaces/')
    user = models.ForeignKey(CustomUser, on_delete=models.CASCADE)

    def __str__(self):
        return str(self.user)

    def delete(self, using=None, keep_parents=False):
        self.test_image.delete()
        return super().delete(using=using, keep_parents=keep_parents)

    def filename(self):
        return os.path.basename(self.test_image.name)


